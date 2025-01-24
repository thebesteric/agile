package io.github.thebesteric.framework.agile.test.service;

import io.github.thebesteric.framework.agile.plugins.mocker.annotation.Mock;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.MockType;
import io.github.thebesteric.framework.agile.test.controller.MockerController;
import io.github.thebesteric.framework.agile.test.mock.MyMocker;
import org.springframework.stereotype.Service;

/**
 * MockService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-24 17:42:10
 */
@Service
public class MockService {

    @Mock(condition = "(#parent.id == 1 || #parent.sub.name == lisi) && #name == zs", type = MockType.CLASS, targetClass = MyMocker.class)
    public String method5(String name, MockerController.Parent parent) {
        return name;
    }

}
