package com.matzip.service;


import com.matzip.dto.UsersFormDto;
import com.matzip.entity.Users;
import com.matzip.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class UsersService implements UserDetailsService {

    @Value("${userImgLocation}")
    private String userImgLocation;
    private final UsersRepository usersRepository;
    private final FileService fileService;


    public Users saveUsers(Users users, MultipartFile userImgFile) throws Exception {
        String oriImgName = userImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //파일 업로드
        if (!StringUtils.isEmpty(oriImgName)) {
            imgName = fileService.uploadFile(userImgLocation, oriImgName, userImgFile.getBytes());
            imgUrl = "/images/users/" + imgName;
        }

        //상품 이미지 정보 저장
        users.setUser_image(imgUrl);

        validateDuplicateUsers(users);
        return usersRepository.save(users);
    }

    public void updateUsers(UsersFormDto usersFormDto, MultipartFile userImgFile) throws Exception {
        String oriImgName = userImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //객체 찾기
        Users users = usersRepository.findByUserid(usersFormDto.getUserid());

        //파일 업로드
        if (!StringUtils.isEmpty(oriImgName)) {
            //1. 사진이 바뀐 경우
            //기존 이미지 물리경로에서 삭제
            imgName = users.getUser_image();
            imgUrl = imgName.substring(imgName.lastIndexOf("/"));
            imgUrl = userImgLocation + imgUrl;

            fileService.deleteFile(imgUrl);

            //바뀐사진 dto에 담기
            imgName = fileService.uploadFile(userImgLocation, oriImgName, userImgFile.getBytes());
            imgUrl = "/images/users/" + imgName;
            usersFormDto.setUser_image(imgUrl);
        } else {
            //2. 사진이 바뀌지 않은 경우(기존 이미지 저장)
            usersFormDto.setUser_image(users.getUser_image());
        }

        //usersFormDto(수정폼 입력 정보)로 data변경
        users.updateUsers(usersFormDto);
        usersRepository.save(users);
    }


    private void validateDuplicateUsers(Users users) {
        Users findUsers = usersRepository.findByUserid(users.getUserid());
        if (findUsers != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {

        Users users = usersRepository.findByUserid(userid);

        if (users == null) {
            throw new UsernameNotFoundException(userid);
        }

        return User.builder()
                .username(users.getUserid())
                .password(users.getUser_pwd())
                .roles(users.getUser_role().toString())
                .build();
    }


    public List<UsersFormDto> findAll() {
        //엔티티리스트 객체를 dto리스트객체로 컨트롤러에게 줘야함. 변환필요 .
        List<Users> usersEntityList = usersRepository.findAll();
        List<UsersFormDto> usersFormDtoList = new ArrayList<>();
        //엔티티가 여러개 담긴 리스트를 dto여러개인 리스트로 .
        //usersEntityList 를 하나씩 usersFormDtoList 로 옮겨줘야함
        for (Users users : usersEntityList) {
            usersFormDtoList.add(UsersFormDto.toUsersDto(users));
        }
        return usersFormDtoList;
    }

    public UsersFormDto findById(String userid) {
        Optional<Users> optionalUsers = usersRepository.findById(userid);
        if (optionalUsers.isPresent()) {
//            Users users = optionalUsers.get();
//            UsersFormDto usersFormDto= UsersFormDto.toUsersDto(users);
//            return usersFormDto;
            return UsersFormDto.toUsersDto(optionalUsers.get());
        } else {
            return null;
        }

    }

    public void deleteById(String userid) {
        usersRepository.deleteById(userid);
    }

    public void updateUserInfo(UsersFormDto usersFormDto) {
        // 여기에서 사용자 정보 업데이트 작업 수행
        // usersFormDto를 엔티티로 변환하여 업데이트할 수 있어야 함
        Users users = Users.aboutUsers(usersFormDto);
        usersRepository.save(users);
    }
    public Users findByUserId(String userid) {
        return usersRepository.findByUserid(userid);
    }
}