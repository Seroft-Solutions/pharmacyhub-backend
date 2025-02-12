package com.pharmacy.hub.service;

import com.pharmacy.hub.dto.PHUserConnectionDTO;
import com.pharmacy.hub.dto.PHUserDTO;
import com.pharmacy.hub.dto.display.UserDisplayDTO;

import java.util.List;

public interface PHUserService
{
  PHUserDTO saveUser(PHUserDTO phUserDTO);

  PHUserDTO updateUser(PHUserDTO phUserDTO);

  PHUserDTO findUser(long id);

  List<UserDisplayDTO> findAllUsers();

//  void connectWith(PHUserConnectionDTO phUserConnectionDTO);

  void disconnectWith(PHUserConnectionDTO phUserConnectionDTO);

  List<UserDisplayDTO> getAllUserConnections();

  void updateState(PHUserConnectionDTO userConnectionDTO);

  void updateNotes(PHUserConnectionDTO userConnectionDTO);

  List getAllConnections();
}
