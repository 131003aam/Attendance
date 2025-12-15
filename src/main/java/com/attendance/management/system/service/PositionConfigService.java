package com.attendance.management.system.service;

import com.attendance.management.system.dao.PositionConfigDAO;
import com.attendance.management.system.entity.PositionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionConfigService {

    @Autowired
    private PositionConfigDAO positionConfigDAO;

    public List<PositionConfig> getAllPositionConfigs() {
        return positionConfigDAO.findAll();
    }

    public PositionConfig getPositionConfigById(String pid) {
        return positionConfigDAO.findById(pid);
    }

    public void createPositionConfig(PositionConfig positionConfig) {
        positionConfigDAO.insert(positionConfig);
    }

    public void updatePositionConfig(PositionConfig positionConfig) {
        positionConfigDAO.update(positionConfig);
    }

    public void deletePositionConfig(String pid) {
        positionConfigDAO.delete(pid);
    }
}




















