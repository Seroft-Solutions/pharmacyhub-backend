package com.pharmacyhub.service;

import com.pharmacyhub.dto.PHUserConnectionDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.display.UserDisplayDTO;

import java.util.List;

public interface PHUserService
{
  PHUserDTO saveUser(PHUserDTO phUserDTO);

  PHUserDTO updateUser(PHUserDTO phUserDTO);

  PHUserDTO findUser(long id);

  List<UserDisplayDTO> findAllUsers();

  void connectWith(PHUserConnectionDTO phUserConnectionDTO);

  void disconnectWith(PHUserConnectionDTO phUserConnectionDTO);

  List<UserDisplayDTO> getAllUserConnections();

  void updateState(PHUserConnectionDTO userConnectionDTO);

  void updateNotes(PHUserConnectionDTO userConnectionDTO);

  List getAllConnections();
}
