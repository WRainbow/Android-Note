// Generated code from Butter Knife. Do not modify!
package com.srainbow.androiddemo2;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding implements Unbinder {
  private MainActivity target;

  private View view2131165218;

  @UiThread
  public MainActivity_ViewBinding(MainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public MainActivity_ViewBinding(final MainActivity target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.bn_main_bn1, "field 'mBnTest1' and method 'on'");
    target.mBnTest1 = Utils.castView(view, R.id.bn_main_bn1, "field 'mBnTest1'", Button.class);
    view2131165218 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.on();
      }
    });
    target.mBnTest2 = Utils.findRequiredViewAsType(source, R.id.bn_main_bn2, "field 'mBnTest2'", Button.class);

    Context context = source.getContext();
    target.color = ContextCompat.getColor(context, R.color.colorAccent);
    target.drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mBnTest1 = null;
    target.mBnTest2 = null;

    view2131165218.setOnClickListener(null);
    view2131165218 = null;
  }
}
