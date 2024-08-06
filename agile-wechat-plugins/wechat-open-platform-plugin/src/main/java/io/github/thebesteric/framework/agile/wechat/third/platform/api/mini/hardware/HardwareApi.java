package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.hardware;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.PkgType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 硬件设备
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 19:54:11
 */
public class HardwareApi {

    /**
     * 发送设备消息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/sendHardwareDeviceMessage.html">发送设备消息</a>
     * @author wangweijun
     * @since 2024/8/6 20:27
     */
    public WechatResponse sendHardwareDeviceMessage(String accessToken, SendHardwareDeviceMessageRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/device/subscribe/send?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 获取设备票据
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetSnTicketResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/getSnTicket.html">获取设备票据</a>
     * @author wangweijun
     * @since 2024/8/6 20:33
     */
    public GetSnTicketResponse getSnTicket(String accessToken, GetSnTicketRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/getsnticket?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GetSnTicketResponse.class);
    }

    /**
     * 创建设备组
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return CreateIotGroupIdResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/createIotGroupId.html">创建设备组</a>
     * @author wangweijun
     * @since 2024/8/6 20:38
     */
    public CreateIotGroupIdResponse createIotGroupId(String accessToken, CreateIotGroupIdRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/group/createid?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, CreateIotGroupIdResponse.class);
    }

    /**
     * 设备组删除设备
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return RemoveIotGroupDeviceResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/removeIotGroupDevice.html">设备组删除设备</a>
     * @author wangweijun
     * @since 2024/8/6 20:44
     */
    public RemoveIotGroupDeviceResponse removeIotGroupDevice(String accessToken, RemoveIotGroupDeviceRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/group/removedevice?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, RemoveIotGroupDeviceResponse.class);
    }

    /**
     * 设备组添加设备
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return AddIotGroupDeviceResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/addIotGroupDevice.html">设备组添加设备</a>
     * @author wangweijun
     * @since 2024/8/6 20:44
     */
    public AddIotGroupDeviceResponse addIotGroupDevice(String accessToken, AddIotGroupDeviceRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/group/adddevice?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, AddIotGroupDeviceResponse.class);
    }

    /**
     * 查询设备组信息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param groupId     设备组的唯一标识
     *
     * @return GetIotGroupInfoResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/getIotGroupInfo.html">查询设备组信息</a>
     * @author wangweijun
     * @since 2024/8/6 20:52
     */
    public GetIotGroupInfoResponse getIotGroupInfo(String accessToken, String groupId) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/group/getinfo?access_token=%s", accessToken, groupId);
        HttpResponse response = HttpUtils.post(url, Map.of("group_id", groupId));
        return BaseResponse.of(response, GetIotGroupInfoResponse.class);
    }

    /**
     * 激活设备 license
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return ActiveLicenseDeviceResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/activeLicenseDevice.html">激活设备 license</a>
     * @author wangweijun
     * @since 2024/8/6 21:02
     */
    public ActiveLicenseDeviceResponse activeLicenseDevice(String accessToken, ActiveLicenseDeviceRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/license/activedevice?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, ActiveLicenseDeviceResponse.class);
    }

    /**
     * 查询设备激活详情
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetLicenseDeviceInfoResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/getLicenseDeviceInfo.html">查询设备激活详情</a>
     * @author wangweijun
     * @since 2024/8/6 21:07
     */
    public GetLicenseDeviceInfoResponse getLicenseDeviceInfo(String accessToken, GetLicenseDeviceInfoRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/license/getdeviceinfo?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GetLicenseDeviceInfoResponse.class);
    }

    /**
     * 查询 license 资源包列表
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param pkgType     资源包类型
     *
     * @return GetLicensePkgListResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/hardware-device/getLicensePkgList.html">查询 license 资源包列表</a>
     * @author wangweijun
     * @since 2024/8/6 21:23
     */
    public GetLicensePkgListResponse getLicensePkgList(String accessToken, PkgType pkgType) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/license/getpkglist?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("pkg_type", pkgType.getCode()));
        return BaseResponse.of(response, GetLicensePkgListResponse.class);
    }
}
