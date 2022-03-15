package com.example.clewapplication;

import android.os.Bundle;
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
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Collections;

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
    private final ArrayList<Node> coordinatesList = new ArrayList<>();
    private final ArrayList<Float> distancesToLineList = new ArrayList<>();

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
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            createModel(anchorNode);
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
        for(Node n : coordinatesList){
            distancesToLineList.add(distanceToLine(fEndpoint, LEndpoint, n));
        }
        distancesToLineList.remove(distancesToLineList.size() - 1);
        distancesToLineList.remove(0);
        ArrayList<Node> waypoints = new ArrayList<>();
        for(Node nn : coordinatesList){
            if(computePath(distancesToLineList, 0.5f).contains(distanceToLine(fEndpoint, LEndpoint, nn))){
                waypoints.add(nn);
            }
        }
        //SAFE DELETE
        for(Node nnn : waypoints){
            nnn.setRenderable(modelRenderable);
        }
    }

    public float distanceToLine(Node aCrumb, Node bCrumb, Node cCrumb){
        Vector3 point1 = aCrumb.getWorldPosition();
        Vector3 point2 = bCrumb.getWorldPosition();
        Vector3 difference = Vector3.subtract(point1, point2);
        Vector3 farPoint = cCrumb.getWorldPosition();
        Vector3 unitVector = difference.normalized();
        Vector3 a = Vector3.subtract(point1, farPoint);
        float magnitudeA = a.length();
        float aDotUnit = Vector3.dot(a,unitVector);
        return (float) (Math.sqrt((magnitudeA)*(magnitudeA) - (aDotUnit)*(aDotUnit)));
    }

    public ArrayList<Float> computePath(ArrayList<Float> arr, float threshold){
        ArrayList<Float> newArr = new ArrayList<>();
        float bigFloat = Collections.max(arr);
        if(bigFloat <= threshold){
            newArr = arr;
        }else{
            for(float f : arr){
                if(f > threshold){
                    newArr.add(f);
                }
            }
        }
        return newArr;
    }
}