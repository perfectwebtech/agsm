package net.myacxy.agsm;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;

import net.myacxy.agsm.activities.MainActivity;
import net.myacxy.agsm.interfaces.DatabaseManager;
import net.myacxy.agsm.interfaces.GameFinder;
import net.myacxy.agsm.interfaces.OnServerUpdatedListener;
import net.myacxy.agsm.interfaces.ServerFinder;
import net.myacxy.agsm.interfaces.ServerManager;
import net.myacxy.agsm.managers.ActiveDatabaseManager;
import net.myacxy.agsm.managers.JgsqServerManager;
import net.myacxy.agsm.models.GameServerEntity;
import net.myacxy.agsm.utils.ActiveServerFinder;
import net.myacxy.agsm.utils.JgsqGameFinder;
import net.myacxy.jgsq.helpers.ServerResponseStatus;
import net.myacxy.jgsq.models.Game;
import net.myacxy.jgsq.models.GameServer;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.List;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

@EService
public class AgsmService extends Service
{
    @Bean(JgsqServerManager.class)      ServerManager serverManager;
    @Bean(ActiveDatabaseManager.class)  DatabaseManager databaseManager;
    @Bean(ActiveServerFinder.class)     ServerFinder serverFinder;
    @Bean(JgsqGameFinder.class)         GameFinder gameFinder;

    private Looper looper;
    private AgsmServiceUpdateServersHandler serviceHandler;

    @Override
    public void onCreate()
    {
        stopService(new Intent(getApplicationContext(), AgsmService.class));
        HandlerThread thread = new HandlerThread("AgsmService", THREAD_PRIORITY_BACKGROUND);
        thread.start();
        looper = thread.getLooper();
        serviceHandler = new AgsmServiceUpdateServersHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        serviceHandler.sendMessage(message);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class AgsmServiceUpdateServersHandler extends Handler implements OnServerUpdatedListener
    {
        private List<GameServerEntity> gameServerEntities;
        private int count = 0;
        private int startId = -1;

        public AgsmServiceUpdateServersHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            startId = msg.arg1;
            gameServerEntities = serverFinder.findAll();
            if(gameServerEntities != null)
            {
                Intent intent = new Intent(MainActivity.ACTION_ON_UPDATE_SERVERS);
                sendBroadcast(intent);

                count = gameServerEntities.size();
                for (GameServerEntity gameServerEntity : gameServerEntities)
                {
                    refreshServer(gameServerEntity);
                }
            }
            else
            {
                count = 0;
                stopSelf(startId);
            }
        } // handleMessage

        @Override
        public void onServerUpdated(GameServer gameServer)
        {
            if (gameServer.getProtocol().getResponseStatus() == ServerResponseStatus.OK)
            {
                databaseManager.update(gameServer);
            }
            else
            {
                System.out.println(gameServer.getProtocol().getResponseStatus().toString());
            }
            if(--count == 0)
            {
                onAllServerUpdated();
            }
        } // onServerUpdated

        private void onAllServerUpdated()
        {
            Intent intent = new Intent(MainActivity.ACTION_ON_SERVERS_UPDATED);
            sendBroadcast(intent);
            stopSelf(startId);
        }

        private void refreshServer(GameServerEntity gameServerEntity)
        {
            Game game = gameFinder.find(gameServerEntity.game.name);

            serverManager.update(
                    game,
                    gameServerEntity.ipAddress,
                    gameServerEntity.port,
                    this
            );
        } // refreshServer
    } // AgsmServiceUpdateServersHandler
} // AgsmService
