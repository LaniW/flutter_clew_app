package com.example.clewapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Objects;

public class SingleUseRouteActivity extends FragmentActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = SingleUseRouteActivity.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static ArFragment arFragment;

    private Session session;
    private ModelRenderable modelRenderable;
    private boolean b = true;
    private boolean buttonStart = false;
    private boolean bPath = true;
    private Node newCrumb = new Node();
    private final ArrayList<Node> coordinatesList = new ArrayList<>();
    private static TextToSpeech tts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_use_route);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        assert arFragment != null;
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        setupModel(); //Rendering Crumbs [SAFE DELETE]

        tts = new TextToSpeech(this, this);
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            Log.e(TAG, "Inside Session Destroyed");
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    private void setupModel() {
        ModelRenderable.builder().setSource(this, R.raw.sphere2).build().thenAccept(renderable -> modelRenderable = renderable).exceptionally(throwable -> {
            Toast.makeText(SingleUseRouteActivity.this, "Model can't be loaded", Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void onUpdateFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        if ((frame.getCamera().getTrackingState() == TrackingState.TRACKING) && buttonStart) {
            path(bPath);

        } else {
            bPath = false;
        }
    }

    public void path(boolean bPath) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        assert frame != null;
        Pose pos = frame.getCamera().getPose().compose(Pose.makeTranslation(0, 0, 0));
        Anchor anchor = Objects.requireNonNull(arFragment.getArSceneView().getSession()).createAnchor(pos);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Node crumb = new Node();
        crumb.setParent(anchorNode);

        double distanceValue = Math.sqrt((crumb.getWorldPosition().x - newCrumb.getWorldPosition().x) * (crumb.getWorldPosition().x - newCrumb.getWorldPosition().x) + (crumb.getWorldPosition().y - newCrumb.getWorldPosition().y) * (crumb.getWorldPosition().y - newCrumb.getWorldPosition().y) + (crumb.getWorldPosition().z - newCrumb.getWorldPosition().z) * (crumb.getWorldPosition().z - newCrumb.getWorldPosition().z));

        if (bPath) {
            if (b || distanceValue >= 0.2) {
                crumb.setRenderable(modelRenderable);
                newCrumb = crumb;

                coordinatesList.add(crumb);
                b = false;
            }
        }
    }

    public void setTrue(View view) {
        buttonStart = true;
        bPath = true;
    }

    public void setFalse(View view) {
        buttonStart = false;
        bPath = false;

        ArrayList<Node> lineWaypoints = new ArrayList<>();

        //simplifies the paths (only creates line segments, not actual waypoints)
        for (Node nn : coordinatesList) {
            rdp(coordinatesList, 0, coordinatesList.size(), 0.5f, lineWaypoints);
        }

        //the notable waypoints (points distinguished from the line segments)
        ArrayList<Node> pathWaypoints = new ArrayList<>();
        for (Node n4 : coordinatesList) {
            if (!lineWaypoints.contains(n4)) {
                pathWaypoints.add(n4);
            }
        }

        //The path waypoints arraylist is 436 nodes long? (when run) but only had a couple of nodes displayed
        for (int l = 0; l < coordinatesList.size() - pathWaypoints.size(); l++) {
            directionToVoice(pathWaypoints.get(l), pathWaypoints.get(l + 1));
        }

        //SAFE DELETE (sets all nodes to a single parent node but also renders it)
        Frame frame2 = arFragment.getArSceneView().getArFrame();
        assert frame2 != null;
        Pose pos = frame2.getCamera().getPose().compose(Pose.makeTranslation(0, 0, 0));
        Anchor anchor2 = Objects.requireNonNull(arFragment.getArSceneView().getSession()).createAnchor(pos);
        AnchorNode anchorNode2 = new AnchorNode(anchor2);
        anchorNode2.setParent(arFragment.getArSceneView().getScene());
        for (Node nnn : pathWaypoints) {
            nnn.setParent(anchorNode2);
            nnn.setRenderable(modelRenderable);
        }
    }

    public static float distanceToLine(Node aCrumb, Node bCrumb, Node cCrumb) {
        Vector3 point1 = aCrumb.getWorldPosition();
        Vector3 point2 = bCrumb.getWorldPosition();
        Vector3 difference = Vector3.subtract(point1, point2);
        Vector3 farPoint = cCrumb.getWorldPosition();
        Vector3 unitVector = difference.normalized();
        Vector3 a = Vector3.subtract(farPoint, point1);
        float magnitudeA = a.length();
        float aDotUnit = Vector3.dot(a, unitVector);
        return (float) (Math.sqrt((magnitudeA) * (magnitudeA) - (aDotUnit) * (aDotUnit)));
    }

    private static void rdp(ArrayList<Node> arr, int s, int e, float threshold, ArrayList<Node> substituteArr) {
        float fmax = 0;
        int index = 0;

        final Node startNode = arr.get(s);
        final Node endNode = arr.get(e - 1);
        for (int i = s + 1; i < e - 1; i++) {
            final Node inBetween = arr.get(i);
            final float d = distanceToLine(startNode, endNode, inBetween);
            if (d > fmax) {
                index = i;
                fmax = d;
            }
        }
        //If max distance is greater than threshold, recursively simplify
        if (fmax > threshold) {
            rdp(arr, s, index + 1, threshold, substituteArr);
            rdp(arr, index, e, threshold, substituteArr);
            substituteArr.remove(substituteArr.size() - 1);
        } else {
            if ((e - 1 - s) > 0) {
                substituteArr.add(arr.get(s));
                substituteArr.add(arr.get(e - 1));
            } else {
                substituteArr.add(arr.get(s));
            }
        }
    }

    public static void directionToVoice(Node pointOne, Node pointTwo) {

        //Difference vector
        Vector3 point1 = pointOne.getWorldPosition();
        Vector3 point2 = pointTwo.getWorldPosition();
        Vector3 difference = Vector3.subtract(point2, point1);

        //-Z axis vector
        Camera arCamera = arFragment.getArSceneView().getScene().getCamera();
        Vector3 cameraPos = arCamera.getWorldPosition();
        Vector3 cameraForward = Vector3.add(cameraPos, arCamera.getForward().normalized());
        Vector3 frontFaceZ = Vector3.subtract(cameraForward, cameraPos);
        frontFaceZ = new Vector3(frontFaceZ.x, 0, frontFaceZ.z).normalized();

        //distance (in meters)
        float distance = (float) (Math.sqrt(Math.pow((frontFaceZ.x - difference.x), 2) +
                Math.pow((frontFaceZ.y - difference.y), 2) +
                Math.pow((frontFaceZ.z - difference.z), 2)));

        //horizontal angle (in radians)
        float horizontalAngle = (float) (Math.atan2((frontFaceZ.y), (frontFaceZ.x)) -
                Math.atan2((difference.y), (difference.x)));

        //vertical angle (in radians)
        float verticalAngle = (float) (
                Math.acos(frontFaceZ.z / (
                        Math.sqrt(
                                Math.pow(frontFaceZ.x, 2) +
                                        Math.pow(frontFaceZ.y, 2) +
                                        Math.pow(frontFaceZ.z, 2)))) -
                        Math.acos((difference.z / (
                                Math.sqrt(
                                        Math.pow(difference.x, 2) +
                                                Math.pow(difference.y, 2) +
                                                Math.pow(difference.z, 2))))));

        if ((point2.y - point1.y) > 0.01) {
            speakOut("go upstairs");
            System.out.println("bananas go upstairs");
        } else if ((point2.y - point1.y) < -0.01) {
            speakOut("go downstairs");
            System.out.println("bananas go downstairs");
        } else {
            if ((point2.z - point1.z) >= 0.03) {
                speakOut("turn around");
                System.out.println("bananas turn around");
            } else {
                if ((point2.x - point1.x) <= -0.01) {
                    speakOut("turn left");
                    System.out.println("bananas turn left");
                } else if ((point2.x - point1.x) >= 0.01) {
                    speakOut("turn right");
                    System.out.println("bananas turn right");
                } else {
                    speakOut("go forwards");
                    System.out.println("bananas go forwards");
                }
            }
        }

        System.out.println("bananas x: " + (point2.x - point1.x));
        System.out.println("bananas y: " + (point2.y - point1.y));
        System.out.println("bananas z: " + (point2.z - point1.z));
    }

    @Override
    public void onInit(int i) {

    }

    private static void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "");
    }
}