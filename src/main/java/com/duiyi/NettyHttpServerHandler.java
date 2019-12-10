package com.duiyi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @time: 2019/11/23 19:12
 * @version: 1.00
 * @author: duiyi
 * <p>
 * 自定义Handler，支持基本的get请求，post请求，数据类型为from表单，Json
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /**
     * 处理请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        //打印请求报文：请求行，请求头，请求空行，请求体
        System.out.println();
        System.out.println(fullHttpRequest);

        FullHttpResponse response = null;
        //请求固定资源
        String str = "/index.html";
        //获取文件的流
        InputStream is = null;
        //Get请求
        if (fullHttpRequest.getMethod().equals(HttpMethod.GET)) {
            //获取请求附带参数
            System.out.println(getGetParamsFromChannel(fullHttpRequest));

            //判断资源 是否存在
            if (str.equals(fullHttpRequest.getUri())) {
                //存在
                is = this.getClass().getResourceAsStream("/index.html");

            } else {
                //404
                is = this.getClass().getResourceAsStream("/404.html");
            }
            String resource = new Scanner(is).useDelimiter("\\Z").next();
            ByteBuf buf = copiedBuffer(resource, CharsetUtil.UTF_8);
            //结果返回客户端
            response = responseOK(HttpResponseStatus.OK, buf);
        }

        //Post请求
        else if (fullHttpRequest.getMethod().equals(HttpMethod.POST)) {
            //获取请求参数
            System.out.println(getPostParamsFromChannel(fullHttpRequest));
            //判断资源 是否存在
            if (str.equals(fullHttpRequest.getUri())) {
                //存在
                is = this.getClass().getResourceAsStream("/index.html");

            } else {
                //404
                is = this.getClass().getResourceAsStream("/404.html");
            }
            String resource = new Scanner(is).useDelimiter("\\Z").next();
            ByteBuf buf = copiedBuffer(resource, CharsetUtil.UTF_8);
            //结果返回客户端
            response = responseOK(HttpResponseStatus.OK, buf);
        }

        //不支持其他请求
        else {
            response = responseOK(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
        }

        // 响应客户端
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 获取GET方式传递的参数
     */
    private Map<String, Object> getGetParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.getMethod().equals(HttpMethod.GET)) {
            // 处理GET请求
            //通过解码器得到客户端uri
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.getUri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 获取POST方式传递的参数
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.getMethod().equals(HttpMethod.POST)) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            //判断请求头参数Content-Type值得是from表单还是json数据
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJsonParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     */
    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }

        return params;
    }

    /**
     * 解析json数据（Content-Type = application/json）
     */
    private Map<String, Object> getJsonParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<String, Object>();

        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, StandardCharsets.UTF_8);

        JSONObject jsonParams = JSONObject.fromObject(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get(key));
        }

        return params;
    }

    //200
    private FullHttpResponse responseOK(HttpResponseStatus status, ByteBuf content) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (content != null) {
            response.headers().set("Content-Type", "text/plain;charset=UTF-8");
            response.headers().set("Content_Length", response.content().readableBytes());
        }
        return response;
    }
}