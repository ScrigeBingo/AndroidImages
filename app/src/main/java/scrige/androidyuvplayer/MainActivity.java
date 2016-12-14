package scrige.androidyuvplayer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    /** The OpenGL view */
    private GLSurfaceView glSurfaceView;

    private GLFrameRenderer glRenderer;

    private Handler mHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (GLES20Support.detectOpenGLES20(this) == false) {
            GLES20Support.getNoSupportGLES20Dialog(this);
        }

        // Initiate the Open GL view and
        // create an instance with this activity
        glSurfaceView = new GLFrameSurface(this);
        glSurfaceView.setEGLContextClientVersion(2);
        //
        glRenderer = new GLFrameRenderer(null, glSurfaceView, getDM(this));
        // set our renderer to be the main renderer with
        // the current activity context
        glSurfaceView.setRenderer(glRenderer);
        //
        setContentView(glSurfaceView);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                glRenderer.update(176, 144);

                byte[] yuv = getFromRaw();

                copyFrom(yuv, 176, 144);

                byte[] y = new byte[yuvPlanes[0].remaining()];
                yuvPlanes[0].get(y, 0, y.length);

                byte[] u = new byte[yuvPlanes[1].remaining()];
                yuvPlanes[1].get(u, 0, u.length);

                byte[] v = new byte[yuvPlanes[2].remaining()];
                yuvPlanes[2].get(v, 0, v.length);


                glRenderer.update(y, u, v);
            }
        }, 1000);
    }

    public DisplayMetrics getDM(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return outMetrics;
    }


    public byte[] getFromRaw() {
        try {
            InputStream in = getResources().openRawResource(R.raw.video); // 从Resources中raw中的文件获取输入流

            int length = in.available();
            byte[] buffer = new byte[length];

            in.read(buffer);
            in.close();

            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ByteBuffer[] yuvPlanes;

    public void copyFrom(byte[] yuvData, int width, int height) {

        int[] yuvStrides = { width, width / 2, width / 2};

        if (yuvPlanes == null) {
            yuvPlanes = new ByteBuffer[3];
            yuvPlanes[0] = ByteBuffer.allocateDirect(yuvStrides[0] * height);
            yuvPlanes[1] = ByteBuffer.allocateDirect(yuvStrides[1] * height / 2);
            yuvPlanes[2] = ByteBuffer.allocateDirect(yuvStrides[2] * height / 2);
        }

        if (yuvData.length < width * height * 3 / 2) {
            throw new RuntimeException("Wrong arrays size: " + yuvData.length);
        }

        int planeSize = width * height;

        ByteBuffer[] planes = new ByteBuffer[3];

        planes[0] = ByteBuffer.wrap(yuvData, 0, planeSize);
        planes[1] = ByteBuffer.wrap(yuvData, planeSize, planeSize / 4);
        planes[2] = ByteBuffer.wrap(yuvData, planeSize + planeSize / 4, planeSize / 4);

        for (int i = 0; i < 3; i++) {
            yuvPlanes[i].position(0);
            yuvPlanes[i].put(planes[i]);
            yuvPlanes[i].position(0);
            yuvPlanes[i].limit(yuvPlanes[i].capacity());
        }
    }

    /**
     * Remember to resume the glSurface
     */
    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    /**
     * Also pause the glSurface
     */
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

}
