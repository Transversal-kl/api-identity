package com.fv.billpay.api.role.repository;

import org.keycloak.representations.idm.GroupRepresentation;
import java.util.List;
import java.util.Optional;

public interface IGroupRepository {
    boolean createGroup(String name, String description);
    boolean updateGroup(String groupId, String newName, String newDescription);
    boolean deleteGroup(String groupId);
    Optional<GroupRepresentation> getGroup(String groupId);
    Optional<GroupRepresentation> getGroupByName(String groupName);
    List<GroupRepresentation> getAllGroups(int first, int max);
    long countGroups();
}
