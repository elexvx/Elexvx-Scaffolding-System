package com.tencent.tdesign.service;

import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.entity.UserParameterEntity;
import com.tencent.tdesign.mapper.UserParameterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class UserParameterService {

    private final UserParameterMapper mapper;

    public UserParameterService(UserParameterMapper mapper) {
        this.mapper = mapper;
    }

    public List<UserParameterEntity> getCurrentUserParameters() {
        long userId = StpUtil.getLoginIdAsLong();
        return mapper.selectByUserId(userId);
    }

    @Transactional
    public UserParameterEntity saveParameter(UserParameterEntity parameter) {
        long userId = StpUtil.getLoginIdAsLong();
        parameter.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (parameter.getId() == null) {
            parameter.setCreatedAt(now);
            parameter.setUpdatedAt(now);
            mapper.insert(parameter);
        } else {
            parameter.setUpdatedAt(now);
            mapper.update(parameter);
        }
        return parameter;
    }

    @Transactional
    public void deleteParameter(Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        UserParameterEntity p = mapper.selectById(id);
        if (p != null && p.getUserId().equals(userId)) {
            mapper.deleteById(id);
        }
    }

    @Transactional
    public UserParameterEntity updateParameter(Long id, UserParameterEntity updated) {
        long userId = StpUtil.getLoginIdAsLong();
        UserParameterEntity p = mapper.selectById(id);
        if (p == null || !p.getUserId().equals(userId)) {
            throw new RuntimeException("Parameter not found or unauthorized");
        }
        p.setParamKey(updated.getParamKey());
        p.setParamValue(updated.getParamValue());
        p.setDescription(updated.getDescription());
        p.setUpdatedAt(LocalDateTime.now());
        mapper.update(p);
        return p;
    }
}
