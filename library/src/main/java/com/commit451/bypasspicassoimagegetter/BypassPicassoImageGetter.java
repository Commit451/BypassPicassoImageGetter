package com.commit451.bypasspicassoimagegetter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import in.uncod.android.bypass.Bypass;

/**
 * Original credits: http://stackoverflow.com/a/25530488/504611
 */
public class BypassPicassoImageGetter implements Bypass.ImageGetter {

    private final Picasso mPicasso;
    private final WeakReference<TextView> mTextView;
    private int maxWidth = -1;

    public BypassPicassoImageGetter(final TextView textView, final Picasso picasso) {
        mTextView = new WeakReference<>(textView);
        mPicasso = picasso;
    }

    @Override
    public Drawable getDrawable(String source) {

        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        final String finalSource = source;
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    return mPicasso.load(finalSource).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                TextView textView = mTextView.get();
                if (textView == null) {
                    return;
                }
                try {
                    if (maxWidth == -1) {
                        int horizontalPadding = textView.getPaddingLeft() + textView.getPaddingRight();
                        maxWidth = textView.getMeasuredWidth() - horizontalPadding;
                        if (maxWidth == 0) {
                            maxWidth = Integer.MAX_VALUE;
                        }
                    }

                    final BitmapDrawable drawable = new BitmapDrawable(textView.getResources(), bitmap);
                    final double aspectRatio = 1.0 * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                    final int width = Math.min(maxWidth, drawable.getIntrinsicWidth());
                    final int height = (int) (width / aspectRatio);

                    drawable.setBounds(0, 0, width, height);

                    result.setDrawable(drawable);
                    result.setBounds(0, 0, width, height);

                    textView.setText(textView.getText()); // invalidate() doesn't work correctly...
                } catch (Exception e) {
                    //do something with this?
                }
            }

        }.execute((Void) null);

        return result;
    }

    private static class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }
}