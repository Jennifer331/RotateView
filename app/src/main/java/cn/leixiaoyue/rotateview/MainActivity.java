package cn.leixiaoyue.rotateview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by 80119424 on 2016/1/14.
 */
public class MainActivity extends Activity{
    private MyView mView = null;
    private Button mBitmapBtn,mMatrixBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        init();
    }

    private void init(){
        mView = (MyView)findViewById(R.id.myview);
        mBitmapBtn = (Button)findViewById(R.id.bitmap);
        mMatrixBtn = (Button)findViewById(R.id.matrix);
        mView.setListener(new MyView.MyViewListener() {
            @Override
            public void animationStarted() {
                mBitmapBtn.setClickable(false);
                mMatrixBtn.setClickable(false);
            }

            @Override
            public void animationEnded() {
                mBitmapBtn.setClickable(true);
                mMatrixBtn.setClickable(true);
            }
        });
    }

    public void rotateBitmap(View view){
        mView.nextState(MyView.ROTATE_BITMAP);
    }

    public void rotateMatrix(View view){
        mView.nextState(MyView.ROTATE_MATRIX);
    }
}
