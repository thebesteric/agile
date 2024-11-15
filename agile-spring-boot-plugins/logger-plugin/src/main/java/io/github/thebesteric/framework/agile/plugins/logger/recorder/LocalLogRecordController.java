package io.github.thebesteric.framework.agile.plugins.logger.recorder;

import io.github.thebesteric.framework.agile.core.domain.R;
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
    public R<LocalLogRecorder.LocalLogRecord> logId(@PathVariable String logId) {
        return R.success(LocalLogRecorder.logId(logId));
    }

    @GetMapping("/track/{trackId}")
    public R<List<LocalLogRecorder.LocalLogRecord>> trackId(@PathVariable String trackId) {
        return R.success(LocalLogRecorder.trackId(trackId));
    }

    @GetMapping("/tag/{tagName}")
    public R<List<LocalLogRecorder.LocalLogRecord>> tagName(@PathVariable String tagName) {
        return R.success(LocalLogRecorder.tagName(tagName));
    }

    @GetMapping("/uri/{uriName}")
    public R<List<LocalLogRecorder.LocalLogRecord>> uriName(@PathVariable String uriName) {
        return R.success(LocalLogRecorder.uriName(uriName));
    }

    @GetMapping("/classify/exception")
    public R<LocalLogRecorder.ExceptionLogInfo> classifyException(@RequestParam(required = false) String name) {
        return R.success(LocalLogRecorder.classifyException(name));
    }

    @GetMapping("/list")
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
    public R<Void> clear() {
        LocalLogRecorder.clear();
        return R.success();
    }

}
