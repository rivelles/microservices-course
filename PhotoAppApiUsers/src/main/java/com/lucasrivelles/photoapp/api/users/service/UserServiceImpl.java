package com.lucasrivelles.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.lucasrivelles.photoapp.api.users.data.AlbumsServiceClient;
import com.lucasrivelles.photoapp.api.users.data.UserEntity;
import com.lucasrivelles.photoapp.api.users.data.UsersRepository;
import com.lucasrivelles.photoapp.api.users.shared.UserDto;
import com.lucasrivelles.photoapp.api.users.ui.model.AlbumResponseModel;

@Service
public class UserServiceImpl implements UserService {
	
	UsersRepository usersRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	//RestTemplate restTemplate;
	Environment env;
	AlbumsServiceClient albumsServiceClient;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public UserServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, AlbumsServiceClient albumsServiceClient, Environment env) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.albumsServiceClient = albumsServiceClient;
		this.env = env;
	}

	@Override
	public UserDto createUser(UserDto userDetails) {

		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);
		usersRepository.save(userEntity);
		
		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);
		
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = usersRepository.findByEmail(username);
		if (userEntity == null) throw new UsernameNotFoundException(username);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = usersRepository.findByEmail(email);
		if (userEntity == null) throw new UsernameNotFoundException(email);
		
		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserById(String userId) {
		UserEntity userEntity = usersRepository.findByUserId(userId);
		if (userEntity == null) throw new UsernameNotFoundException(userId);
		
		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		/*
		String albumsUrl = String.format(env.getProperty("albums.url"), userId);
		ResponseEntity<List<AlbumResponseModel>> albumsResponse = restTemplate.exchange(albumsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<AlbumResponseModel>>() {
		});
		 */
		logger.info("Before calling microservice");
		List<AlbumResponseModel> albums = albumsServiceClient.getAlbums(userId);
		
		userDto.setAlbums(albums);
		
		return userDto;
	}

}
