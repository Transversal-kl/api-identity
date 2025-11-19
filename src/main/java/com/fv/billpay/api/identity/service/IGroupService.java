package com.fv.billpay.api.identity.service;

import com.fv.billpay.api.identity.dto.request.GroupRequestDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;
import com.fv.billpay.api.identity.dto.response.GroupResponseDto;

public interface IGroupService {
    GroupResponseDto create(GroupRequestDto dto);
    GroupResponseDto update(String groupId, GroupRequestDto dto);
    boolean delete(String groupId);
    GroupResponseDto getById(String groupId);
    GroupResponseDto getByName(String groupName);
    PagedResponse<GroupResponseDto> getAll(int page, int size);
}
