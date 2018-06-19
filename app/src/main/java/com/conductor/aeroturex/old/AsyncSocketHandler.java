package com.conductor.aeroturex.old;

import org.json.JSONObject;

public interface AsyncSocketHandler {

    void didConnect();

    void didReceiveData(JSONObject data);

    void didDisconnect(Exception error);
}
