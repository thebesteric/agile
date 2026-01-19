package io.github.thebesteric.framework.agile.plugins.logger.recorder;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本地日志控制器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-31 15:44:12
 */
@RestController
@RequestMapping("/agile/logger/local")
@Tag(name = "日志查询")
@RequiredArgsConstructor
public class LocalLogRecordController {

    private final AgileLoggerProperties agileLoggerProperties;

    @GetMapping("/config")
    @Operation(summary = "获取本地日志记录器配置")
    public R<AgileLoggerProperties.LocalLogRecorderConfig> getConfig() {
        return R.success(agileLoggerProperties.getLocalLogRecorderConfig());
    }

    @GetMapping("/log/{logId}")
    @Operation(summary = "根据 logId 查询日志")
    @Parameter(name = "logId", description = "日志 ID", in = ParameterIn.PATH)
    public R<LocalLogRecorder.LocalLogRecord> logId(@PathVariable String logId) {
        return R.success(LocalLogRecorder.logId(logId));
    }

    @GetMapping("/track/{trackId}")
    @Operation(summary = "根据 trackId 查询日志")
    @Parameter(name = "trackId", description = "链路 ID", in = ParameterIn.PATH)
    public R<List<LocalLogRecorder.LocalLogRecord>> trackId(@PathVariable String trackId) {
        return R.success(LocalLogRecorder.trackId(trackId));
    }

    @GetMapping("/tag/{tagName}")
    @Operation(summary = "根据 tagName 查询日志")
    @Parameter(name = "tagName", description = "标签名称", in = ParameterIn.PATH)
    public R<List<LocalLogRecorder.LocalLogRecord>> tagName(@PathVariable String tagName) {
        return R.success(LocalLogRecorder.tagName(tagName));
    }

    @GetMapping("/uri/{uriName}")
    @Operation(summary = "根据 uriName 查询日志")
    @Parameter(name = "uriName", description = "URI", in = ParameterIn.PATH)
    public R<List<LocalLogRecorder.LocalLogRecord>> uriName(@PathVariable String uriName) {
        return R.success(LocalLogRecorder.uriName(uriName));
    }

    @GetMapping("/classify/exception")
    @Operation(summary = "根据 exceptionClassName 查询日志")
    @Parameter(name = "exceptionClassName", description = "异常类名称")
    public R<LocalLogRecorder.ExceptionLogInfo> classifyException(@RequestParam(required = false) String exceptionClassName) {
        return R.success(LocalLogRecorder.classifyException(exceptionClassName));
    }

    @GetMapping("/list")
    @Operation(summary = "日志列表")
    @Parameter(name = "logLevel", description = "日志级别")
    @Parameter(name = "tagName", description = "标签名称")
    @Parameter(name = "current", description = "当前页", schema = @Schema(type = "integer", defaultValue = "1"))
    @Parameter(name = "size", description = "每页大小", schema = @Schema(type = "integer", defaultValue = "10"))
    public R<PagingResponse<LocalLogRecorder.LocalLogRecord>> list(@RequestParam(required = false) String logLevel,
                                                                   @RequestParam(required = false) String tagName,
                                                                   @RequestParam(required = false, defaultValue = "1") Integer current,
                                                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        LogLevel level = null;
        if (StringUtils.isNotEmpty(logLevel)) {
            level = LogLevel.ofWithException(logLevel);
        }
        return R.success(LocalLogRecorder.list(level, tagName, current, size));
    }

    @GetMapping("/clear")
    @Operation(summary = "清空日志")
    public R<Void> clear() {
        LocalLogRecorder.clear();
        return R.success();
    }

}
