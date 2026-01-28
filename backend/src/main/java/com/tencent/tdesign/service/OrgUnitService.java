package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.OrgUnitUpsertRequest;
import com.tencent.tdesign.entity.OrgUnitEntity;
import com.tencent.tdesign.entity.UserEntity;
import com.tencent.tdesign.enums.OrgUnitType;
import com.tencent.tdesign.mapper.OrgUnitLeaderMapper;
import com.tencent.tdesign.mapper.OrgUnitMapper;
import com.tencent.tdesign.mapper.UserMapper;
import com.tencent.tdesign.vo.OrgUnitNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgUnitService {
  private final OrgUnitMapper orgUnitMapper;
  private final OrgUnitLeaderMapper leaderMapper;
  private final UserMapper userMapper;

  public OrgUnitService(OrgUnitMapper orgUnitMapper, OrgUnitLeaderMapper leaderMapper, UserMapper userMapper) {
    this.orgUnitMapper = orgUnitMapper;
    this.leaderMapper = leaderMapper;
    this.userMapper = userMapper;
  }

  public List<OrgUnitNode> tree() {
    List<OrgUnitEntity> entities = orgUnitMapper.selectAll();
    Map<Long, OrgUnitNode> nodeMap = new LinkedHashMap<>();
    for (OrgUnitEntity entity : entities) {
      nodeMap.put(entity.getId(), toNode(entity));
    }

    attachLeaderInfo(nodeMap);

    List<OrgUnitNode> roots = new ArrayList<>();
    for (OrgUnitNode node : nodeMap.values()) {
      if (node.getParentId() == null || node.getParentId() == 0) {
        roots.add(node);
      } else {
        OrgUnitNode parent = nodeMap.get(node.getParentId());
        if (parent != null) {
          parent.getChildren().add(node);
        } else {
          roots.add(node);
        }
      }
    }
    return roots;
  }

  public OrgUnitNode get(long id) {
    OrgUnitEntity entity = orgUnitMapper.selectById(id);
    if (entity == null) throw new IllegalArgumentException("机构不存在");
    OrgUnitNode node = toNode(entity);
    Map<Long, OrgUnitNode> nodeMap = Map.of(id, node);
    attachLeaderInfo(nodeMap);
    return node;
  }

  @Transactional
  public OrgUnitNode create(OrgUnitUpsertRequest req) {
    OrgUnitEntity entity = new OrgUnitEntity();
    apply(entity, req);
    orgUnitMapper.insert(entity);
    replaceLeaders(entity.getId(), req.getLeaderIds());
    return get(entity.getId());
  }

  @Transactional
  public OrgUnitNode update(long id, OrgUnitUpsertRequest req) {
    OrgUnitEntity entity = orgUnitMapper.selectById(id);
    if (entity == null) throw new IllegalArgumentException("机构不存在");
    apply(entity, req);
    orgUnitMapper.update(entity);
    replaceLeaders(id, req.getLeaderIds());
    return get(id);
  }

  @Transactional
  public boolean delete(long id) {
    if (orgUnitMapper.countChildren(id) > 0) {
      throw new IllegalArgumentException("请先删除子级机构");
    }
    leaderMapper.deleteByOrgUnitId(id);
    orgUnitMapper.deleteById(id);
    return true;
  }

  private void apply(OrgUnitEntity entity, OrgUnitUpsertRequest req) {
    if (req.getParentId() != null) entity.setParentId(req.getParentId());
    entity.setName(req.getName());
    entity.setShortName(req.getShortName());
    OrgUnitType type = OrgUnitType.fromValue(req.getType());
    if (type == null) throw new IllegalArgumentException("机构类型不合法");
    entity.setType(type.name());
    entity.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
    entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    entity.setPhone(req.getPhone());
    entity.setEmail(req.getEmail());
  }

  private OrgUnitNode toNode(OrgUnitEntity entity) {
    OrgUnitNode node = new OrgUnitNode();
    node.setId(entity.getId());
    node.setParentId(entity.getParentId());
    node.setName(entity.getName());
    node.setShortName(entity.getShortName());
    node.setType(entity.getType());
    OrgUnitType type = OrgUnitType.fromValue(entity.getType());
    node.setTypeLabel(type == null ? entity.getType() : type.getLabel());
    node.setSortOrder(entity.getSortOrder());
    node.setStatus(entity.getStatus());
    node.setPhone(entity.getPhone());
    node.setEmail(entity.getEmail());
    node.setCreatedAt(entity.getCreatedAt());
    node.setUpdatedAt(entity.getUpdatedAt());
    return node;
  }

  private void replaceLeaders(Long orgUnitId, List<Long> leaderIds) {
    leaderMapper.deleteByOrgUnitId(orgUnitId);
    if (leaderIds == null || leaderIds.isEmpty()) return;
    List<Long> cleaned = leaderIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      leaderMapper.insertLeaders(orgUnitId, cleaned);
    }
  }

  private void attachLeaderInfo(Map<Long, OrgUnitNode> nodes) {
    if (nodes.isEmpty()) return;
    Map<Long, List<Long>> leaderMap = new HashMap<>();
    Set<Long> userIds = new HashSet<>();
    for (Long id : nodes.keySet()) {
      List<Long> leaders = leaderMapper.selectLeaderIds(id);
      if (leaders == null || leaders.isEmpty()) continue;
      leaderMap.put(id, leaders);
      userIds.addAll(leaders);
    }
    Map<Long, String> userNameMap = new HashMap<>();
    if (!userIds.isEmpty()) {
      List<UserEntity> users = userMapper.selectByIds(userIds);
      for (UserEntity user : users) {
        if (user.getId() != null) {
          userNameMap.put(user.getId(), user.getName());
        }
      }
    }
    for (Map.Entry<Long, OrgUnitNode> entry : nodes.entrySet()) {
      List<Long> leaders = leaderMap.get(entry.getKey());
      if (leaders == null) continue;
      OrgUnitNode node = entry.getValue();
      node.setLeaderIds(leaders);
      List<String> leaderNames = leaders.stream()
        .map(userNameMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
      node.setLeaderNames(leaderNames);
    }
  }
}
