package net.myacxy.agsm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.myacxy.agsm.utils.SharedItems;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;

@EReceiver
public class BootReceiver extends BroadcastReceiver
{
    @ReceiverAction(actions = Intent.ACTION_BOOT_COMPLETED)
    void bootCompleted(Context context)
    {
        context.sendBroadcast(
                new Intent(
                        context,
                        PeriodicRefreshReceiver_.class
                ).setAction(SharedItems.Action.Service.ENSURE_PERIODIC_REFRESH)
        );
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // empty, will be overridden in generated subclass
    }
}
