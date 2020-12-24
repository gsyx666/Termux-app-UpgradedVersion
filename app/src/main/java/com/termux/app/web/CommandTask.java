package main.java.com.termux.app.web;


import android.content.Context;
import android.os.AsyncTask;

import com.termux.R;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import main.java.com.termux.utils.UUtils;

public class CommandTask extends AsyncTask {

    public final static String CHANGE_PERMISSION = "/system/bin/chmod 755 ";
    public final static String COMMAND_EXECUTED = "command_executed";

    private List<String> mCommandList;
    private boolean enableSU;
    private int command_executed;

    public CommandTask() {

    }



    public static CommandTask createForConnect(final Context c, final String execName, final String bindPort) {
    	return createForConnect(c, execName, bindPort, true);
    }
    public static CommandTask createForConnect(final Context c, final String execName, final String bindPort, final boolean createDialog) {
        List<String> command = Collections.unmodifiableList(new ArrayList<String>() {
            {
                add(CHANGE_PERMISSION.concat(Constants.INTERNAL_LOCATION + "/scripts/server-sh.sh"));
                add(String.format("%s/scripts/server-sh.sh %s %s", Constants.INTERNAL_LOCATION, execName, bindPort));
            }
        });
        CommandTask task = new CommandTask();
        task.addCommand(command);
        task.setNotification(R.string.web服务正在运行);
        return task;
    }

    public static CommandTask createForDisconnect(Context c) {
        List<String> command = Collections.unmodifiableList(new ArrayList<String>() {
            {
                add(Constants.INTERNAL_LOCATION + "/scripts/shutdown-sh.sh");
            }
        });
        CommandTask task = new CommandTask();
        task.addCommand(command);
        return task;
    }

    public static CommandTask createForUninstall(final Context c) {
        List<String> command = Collections.unmodifiableList(new ArrayList<String>() {
            {
                add(String.format("rm -R %s", Constants.INTERNAL_LOCATION.concat("/")));
            }
        });
        CommandTask task = new CommandTask();
        task.addCommand(command);
        task.setNotification(R.string.web服务正在运行);
        return task;
    }

    @Override
    protected String doInBackground(Object... cmdArgs) {
        checkFilesystem();
        String command[] = mCommandList.toArray(new String[mCommandList.size()]);
        String shell = enableSU ? "su" : "/data/data/com.termux/files/usr/bin/bash";
        List<String> res = Shell.run(shell, command, null, true);
        for (String queryRes : res)
            publishProgress(queryRes);
        publishProgress(COMMAND_EXECUTED);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object s) {
        super.onPostExecute(s);

    }

    @Override
    protected void onProgressUpdate(Object... queryRes) {
        super.onProgressUpdate(queryRes);
        UUtils.showLog("运行:"+ (String) queryRes[0]);

    }

    public CommandTask addCommand(List<String> commandList) {
        this.mCommandList = commandList;
        return this;
    }

    public boolean isEnabledSU() {
        return enableSU;
    }

    public CommandTask enableSU(final boolean enableSU) {
        this.enableSU = enableSU;
        return this;
    }

    public CommandTask toggleEnableSU() {
        return enableSU(!enableSU);
    }

    protected CommandTask setNotification(int resId) {
        command_executed = resId;
        return this;
    }

    protected void checkFilesystem() {
        List<String> listFiles = Collections.unmodifiableList(new ArrayList<String>() {
            {
                add("/logs");
                add("/conf");
                add("/conf/nginx/logs");
                add("/hosts/nginx");
                add("/hosts/lighttpd");
                add("/sessions");
                add("/packages");
                add("/htdocs");
                add("/tmp");
            }
        });

        for (String filePath : listFiles) {
            File f = new File(Constants.PROJECT_LOCATION.concat(filePath));
            if (!f.exists()) f.mkdirs();
        }
    }

}
