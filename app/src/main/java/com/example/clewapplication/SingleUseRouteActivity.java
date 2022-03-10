package com.example.clewapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Vector;

public class SingleUseRouteActivity extends FragmentActivity {

    private static final String TAG = SingleUseRouteActivity.class.getSimpleName();

    private ArFragment arFragment;

    private Session session;
    private ModelRenderable modelRenderable;
    private boolean b = true;
    private boolean buttonStart = false;
    private boolean bPath = true;
    private Node newCrumb = new Node();
    private Node fEndpoint = new Node();
    private Node LEndpoint = new Node();
    private Node node2 = new Node();
    private Node node3 = new Node();
    private ArrayList<Node> coordinatesList = new ArrayList<>();
    private ArrayList<Float> distancesToLineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_use_route);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        setupModel(); //Rendering Crumbs [SAFE DELETE]
        setUpPlane();
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

    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
            }
        });
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode((arFragment.getTransformationSystem()));
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
    }

    private void onUpdateFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        if ((frame.getCamera().getTrackingState() == TrackingState.TRACKING) && buttonStart) {
            path(bPath);

        }else{
            addLineBetweenHits(fEndpoint, LEndpoint);
            node2 = node3;
            bPath = false;
        }
    }

    public void path(boolean bPath) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        Pose pos = frame.getCamera().getPose().compose(Pose.makeTranslation(0, 0, 0));
        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pos);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Node crumb = new Node();
        crumb.setParent(anchorNode);

        double distanceValue = Math.sqrt((crumb.getWorldPosition().x - newCrumb.getWorldPosition().x) * (crumb.getWorldPosition().x - newCrumb.getWorldPosition().x) + (crumb.getWorldPosition().y - newCrumb.getWorldPosition().y) * (crumb.getWorldPosition().y - newCrumb.getWorldPosition().y) + (crumb.getWorldPosition().z - newCrumb.getWorldPosition().z) * (crumb.getWorldPosition().z - newCrumb.getWorldPosition().z));

        if (bPath) {
            if (b || distanceValue >= 0.5) {
                crumb.setRenderable(modelRenderable);
                newCrumb = crumb;

                coordinatesList.add(crumb);
                for (Node n : coordinatesList) {
                    fEndpoint = coordinatesList.get(0);
                    LEndpoint = coordinatesList.get(coordinatesList.size() - 1);
                }
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
    }

    public void addLineBetweenHits(Node aCrumb, Node bCrumb) {

            Vector3 point1, point2;
            point1 = aCrumb.getWorldPosition();
            point2 = bCrumb.getWorldPosition();

            final Vector3 difference = Vector3.subtract(point1, point2);
            final Vector3 directionFromTopToBottom = difference.normalized();
            final Quaternion rotationFromAToB =
                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                    .thenAccept(
                            material -> {
                                ModelRenderable model = ShapeFactory.makeCube(new Vector3(.01f, .01f, difference.length()), Vector3.zero(), material);
                                Node node1 = new Node();
                                node1.setParent(bCrumb);
                                node1.setRenderable(model); //Rendering Lines [SAFE DELETE]***
                                node1.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                node1.setWorldRotation(rotationFromAToB);
                                node3 = node1;
                            }
                    );
    }

    //TODO: Make function to calculate the distances for the lines through vectors
    public void calculateDistance(Node aCrumb, Node bCrumb){
        Vector3 point1 = aCrumb.getWorldPosition();
        Vector3 point2 = bCrumb.getWorldPosition();
        Vector3 difference = Vector3.subtract(point1, point2);
        Vector3 normalVector = difference.normalized();
        Vector3 unitVector;
    }
}