package com.example.bingostar.sendit;

/**
 * Created by bingostar on 2016-10-19.
 */
public interface RequestResponse {
        void processLogin(Boolean resp);
        void processIp(String ip);
        void processRegister(Boolean resp);
}
