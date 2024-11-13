package io.github.thebesteric.framework.agile.plugins.logger.recorder;

import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
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
public class LocalLogRecordController {

    @GetMapping("/log/{logId}")
    public LocalLogRecorder.LocalLogRecord logId(@PathVariable String logId) {
        return LocalLogRecorder.logId(logId);
    }

    @GetMapping("/track/{trackId}")
    public List<LocalLogRecorder.LocalLogRecord> trackId(@PathVariable String trackId) {
        return LocalLogRecorder.trackId(trackId);
    }

    @GetMapping("/tag/{tagName}")
    public List<LocalLogRecorder.LocalLogRecord> tagName(@PathVariable String tagName) {
        return LocalLogRecorder.tagName(tagName);
    }

    @GetMapping("/classify/exception")
    public LocalLogRecorder.ExceptionLogInfo classifyException(@RequestParam(required = false) String name) {
        return LocalLogRecorder.classifyException(name);
    }

    @GetMapping("/list")
    public PagingResponse<LocalLogRecorder.LocalLogRecord> list(@RequestParam(required = false) String logLevel,
                                                                @RequestParam(required = false) String tagName,
                                                                @RequestParam(required = false, defaultValue = "1") Integer current,
                                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        LogLevel level = null;
        if (StringUtils.isNotEmpty(logLevel)) {
            level = LogLevel.ofWithException(logLevel);
        }
        return LocalLogRecorder.list(level, tagName, current, size);
    }

}
