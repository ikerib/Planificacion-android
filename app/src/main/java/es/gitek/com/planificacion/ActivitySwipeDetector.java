package es.gitek.com.planificacion;

/**
 * Created by ikerib on 22/10/14.
 */
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ActivitySwipeDetector implements View.OnTouchListener
{
    static final String logTag = "SwipeDetector";
    private SwipeInterface activity;
    private float downX, downY;
    private long timeDown;
    private final float MIN_DISTANCE;
    private final int VELOCITY;
    private final float MAX_OFF_PATH;

    public ActivitySwipeDetector(Context context, SwipeInterface activity)
    {
        this.activity = activity;
        final ViewConfiguration vc = ViewConfiguration.get(context);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        MIN_DISTANCE = vc.getScaledPagingTouchSlop() * dm.density;
        VELOCITY = vc.getScaledMinimumFlingVelocity();
        MAX_OFF_PATH = MIN_DISTANCE * 2;
    }

    public void onRightToLeftSwipe(View v)
    {
        Log.i(logTag, "RightToLeftSwipe!");
        activity.onRightToLeft(v);
    }

    public void onLeftToRightSwipe(View v)
    {
        Log.i(logTag, "LeftToRightSwipe!");
        activity.onLeftToRight(v);
    }

    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                Log.d("onTouch", "ACTION_DOWN");
                timeDown = System.currentTimeMillis();
                downX = event.getX();
                downY = event.getY();
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }

            case MotionEvent.ACTION_MOVE:
            {
                float y_up = event.getY();
                float deltaY = y_up - downY;
                float absDeltaYMove = Math.abs(deltaY);

                if (absDeltaYMove > 60)
                {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                else
                {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
            }

            break;

            case MotionEvent.ACTION_UP:
            {
                Log.d("onTouch", "ACTION_UP");
                long timeUp = System.currentTimeMillis();
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float absDeltaX = Math.abs(deltaX);
                float deltaY = downY - upY;
                float absDeltaY = Math.abs(deltaY);

                long time = timeUp - timeDown;

                if (absDeltaY > MAX_OFF_PATH)
                {
                    Log.e(logTag, String.format(
                            "absDeltaY=%.2f, MAX_OFF_PATH=%.2f", absDeltaY,
                            MAX_OFF_PATH));
                    return v.performClick();
                }

                final long M_SEC = 1000;
                if (absDeltaX > MIN_DISTANCE && absDeltaX > time * VELOCITY / M_SEC)
                {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if (deltaX < 0)
                    {
                        this.onLeftToRightSwipe(v);
                        return true;
                    }
                    if (deltaX > 0)
                    {
                        this.onRightToLeftSwipe(v);
                        return true;
                    }
                }
                else
                {
                    Log.i(logTag,
                            String.format(
                                    "absDeltaX=%.2f, MIN_DISTANCE=%.2f, absDeltaX > MIN_DISTANCE=%b",
                                    absDeltaX, MIN_DISTANCE,
                                    (absDeltaX > MIN_DISTANCE)));
                    Log.i(logTag,
                            String.format(
                                    "absDeltaX=%.2f, time=%d, VELOCITY=%d, time*VELOCITY/M_SEC=%d, absDeltaX > time * VELOCITY / M_SEC=%b",
                                    absDeltaX, time, VELOCITY, time * VELOCITY
                                            / M_SEC, (absDeltaX > time * VELOCITY
                                            / M_SEC)));
                }

                v.getParent().requestDisallowInterceptTouchEvent(false);

            }
        }
        return false;
    }
    public interface SwipeInterface
    {

        public void onLeftToRight(View v);

        public void onRightToLeft(View v);
    }

}