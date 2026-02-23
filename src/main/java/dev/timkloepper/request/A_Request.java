package dev.timkloepper.request;


import java.nio.ByteBuffer;


public abstract class A_Request {


    protected abstract ByteBuffer p_encode();
    protected abstract void p_decode(ByteBuffer body);


}