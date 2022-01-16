package com.example.clewapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
//Change the Gradle file for the below imports
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SingleUseRouteActivity extends AppCompatActivity {

    private static final String TAG = SingleUseRouteActivity.class.getSimpleName();

    private ArFragment arFragment;

    private Session session;
    private ModelRenderable modelRenderable;

    //Add ARMode, Look through InstantPlacementPoint.TrackingMethod

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_use_route);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        setupModel();
        setUpPlane();
    }

    @Override
    protected void onDestroy() {
        if(session != null) {
            Log.e(TAG, "Inside Session Destroyed");
            session.close();
            session=  null;
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

    private void createModel(AnchorNode anchorNode){
        TransformableNode node = new TransformableNode((arFragment.getTransformationSystem()));
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
    }

    private void onUpdateFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        //Making sure ARCore is tracking some feature points, makes the augmentation little stable.
        if(frame.getCamera().getTrackingState()== TrackingState.TRACKING) {

            Pose pos = frame.getCamera().getPose().compose(Pose.makeTranslation(0, 0, 0));
            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pos);
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Create the arrow node and add it to the anchor.
            Node arrow = new Node();
            arrow.setParent(anchorNode);
            arrow.setRenderable(modelRenderable);
        }
    }

    /*
    get the create arrow node
    run it somehow in the same method
        or if not, in another
    still somehow get it to run in onCreate
     */
}