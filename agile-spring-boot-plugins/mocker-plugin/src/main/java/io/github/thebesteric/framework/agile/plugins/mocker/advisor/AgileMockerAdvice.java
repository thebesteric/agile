package io.github.thebesteric.framework.agile.plugins.mocker.advisor;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.commons.util.ConditionMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.annotation.Mock;
import io.github.thebesteric.framework.agile.plugins.mocker.config.AgileMockerContext;
import io.github.thebesteric.framework.agile.plugins.mocker.config.AgileMockerProperties;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.EmptyMocker;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.MockType;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.Mocker;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AgileMockerAdvice
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:28:11
 */
@RequiredArgsConstructor
public class AgileMockerAdvice implements MethodInterceptor {

    private final AgileMockerContext context;

    private static final Pattern URL_PATTERN = Pattern.compile("^(http|https)://");
    private static final Pattern FILE_PATTERN = Pattern.compile("^(classpath|file):");

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        AgileMockerProperties properties = context.getProperties();
        if (!properties.isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        Method method = invocation.getMethod();
        Mock mockAnno = method.getAnnotation(Mock.class);

        // 不在指定环境中
        if (!inActiveProfiles(mockAnno)) {
            return invocation.proceed();
        }

        String condition = mockAnno.condition();
        Parameter[] parameters = method.getParameters();
        Object[] arguments = invocation.getArguments();

        // 条件判断
        if (ConditionMatcher.match(condition, parameters, arguments)) {
            MockType mockType = mockAnno.type();
            // Class 类型
            if (MockType.CLASS == mockType) {
                return handlerMockClassType(invocation);
            }
            // File 类型
            else if (MockType.FILE == mockType) {
                return handlerMockFileType(invocation);
            }
            // URL 类型
            else if (MockType.URL == mockType) {
                return handlerMockUrlType(invocation);
            }

            throw new IllegalArgumentException("Unsupported MockType: " + mockType);
        }
        return invocation.proceed();
    }

    private Object handlerMockUrlType(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Mock mockAnno = method.getAnnotation(Mock.class);

        String path = mockAnno.path();
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("The path of the mock file cannot be empty");
        }

        // 判断是否是 http:// 或 https:// 开头
        String errorMessage = null;
        if (URL_PATTERN.matcher(path).find()) {
            HttpResponse httpResponse = HttpUtil.createGet(path)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .timeout(1000 * 10)
                    .setConnectionTimeout(1000 * 10)
                    .setReadTimeout(1000 * 10)
                    .execute();

            try (httpResponse) {
                if (httpResponse.isOk()) {
                    String response = httpResponse.body();
                    if (String.class == method.getReturnType()) {
                        return response;
                    }
                    return JSONUtil.toBean(response, method.getReturnType());
                }
                errorMessage = httpResponse.body();
            }
        }

        if (errorMessage != null) {
            throw new IllegalArgumentException("The path of the mock URL is invalid, just support GET method, error message: " + errorMessage);
        }

        throw new IllegalArgumentException("The path of the mock URL must start with 'http(s)://'");
    }

    private Object handlerMockFileType(MethodInvocation invocation) throws IOException {
        Method method = invocation.getMethod();
        Mock mockAnno = method.getAnnotation(Mock.class);

        String path = mockAnno.path();
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("The path of the mock file cannot be empty");
        }

        // 判断是否是 file: 或 classpath: 开头
        if (FILE_PATTERN.matcher(path).find()) {
            StringBuilder sb = new StringBuilder();
            // 读取文件系统下的文件
            if (path.startsWith("file:")) {
                URI uri = URI.create(path);
                File file = new File(uri);
                // 创建文件输入流和缓冲读取器
                try (FileInputStream in = new FileInputStream(file); BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
            }
            // 读取类路径下的文件
            else {
                if (path.startsWith("classpath:")) {
                    path = path.substring("classpath:".length());
                }
                ClassPathResource resource = new ClassPathResource(path);
                try (InputStream in = resource.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
            }
            if (String.class == method.getReturnType()) {
                return sb.toString();
            }
            return JSONUtil.toBean(sb.toString(), method.getReturnType());
        }

        throw new IllegalArgumentException("The path of the mock file must start with 'file:' or 'classpath:'");
    }


    /**
     * 处理 Class 类型的 Mock
     *
     * @param invocation invocation
     *
     * @return Object
     *
     * @author wangweijun
     * @since 2025/1/24 13:58
     */
    private Object handlerMockClassType(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Mock mockAnno = method.getAnnotation(Mock.class);

        Class<? extends Mocker<?>> targetClass = mockAnno.targetClass();
        if (targetClass == EmptyMocker.class) {
            throw new IllegalArgumentException("The targetClass of the mock cannot be empty");
        }

        Parameter[] parameters = method.getParameters();
        Object[] arguments = invocation.getArguments();
        Method mockMethod = getMockMethod(targetClass);

        // 判断原方法的返回值类型是否与 mock 方法的返回值类型相同
        if (method.getReturnType().isAssignableFrom(mockMethod.getReturnType()) || Object.class == mockMethod.getReturnType()) {
            // 判断是否是 Spring 管理的 bean
            Mocker<?> instance = context.getBeanOrDefault(targetClass, null);
            if (instance == null) {
                instance = targetClass.getDeclaredConstructor().newInstance();
            }
            // 执行 mock 方法
            return mockMethod.invoke(instance, method, parameters, arguments);
        }
        throw new IllegalArgumentException("The return type of the mock method must be the same as the return type of the original method");
    }

    /**
     * 判断环境是否匹配
     *
     * @param mockAnno @Mock 注解
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2025/1/23 19:20
     */
    private boolean inActiveProfiles(Mock mockAnno) {
        AgileMockerProperties properties = context.getProperties();
        Set<String> globalEnvs = new HashSet<>(properties.getEnvs());
        Set<String> methodEnvs = new HashSet<>(Arrays.asList(mockAnno.envs()));
        // 取 globalEnvs 和 methodEnvs 的并集
        Set<String> envs = Stream.concat(globalEnvs.stream(), methodEnvs.stream()).collect(Collectors.toSet());
        // 当前环境
        String[] activeProfiles = context.getActiveProfiles();
        // 没有指定环境
        if ((activeProfiles == null || activeProfiles.length == 0) && envs.isEmpty()) {
            return true;
        }
        if (activeProfiles != null && activeProfiles.length > 0 && !envs.isEmpty()) {
            return Stream.of(activeProfiles).anyMatch(envs::contains);
        }
        return false;
    }

    @SneakyThrows
    private Method getMockMethod(Class<? extends Mocker<?>> targetClass) {
        return targetClass.getMethod("mock", Method.class, Parameter[].class, Object[].class);
    }

}
