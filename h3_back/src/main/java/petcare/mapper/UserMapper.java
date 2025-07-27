package petcare.mapper;

import java.util.List;

// import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
// import org.apache.ibatis.annotations.Select;
import petcare.dto.UserDTO;


@Mapper
public interface UserMapper {
    // @Select("SELECT * FROM user WHERE user_id = #{userId}")
    UserDTO findByUserId(@Param("userId") String userId);

    UserDTO findByUserIdAndProvider(@Param("userId") String userId, @Param("provider") String provider);

    // 이메일로 사용자 조회 (중복 체크용)
    UserDTO findByEmail(@Param("email") String email);

    // (이메일, provider)로 사용자 조회
    UserDTO findByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

    // @Insert("INSERT INTO user (user_id, password, email, phone, name, role, created_at) " +
    //         "VALUES (#{userId}, #{password}, #{email}, #{phone}, #{name}, #{role}, NOW())")
    void insertUser(UserDTO user);

    // 소셜로그인 사용자 insert (password 없이)
    void insertSocialUser(UserDTO user);
    
    // 소셜로그인 정보 업데이트
    void updateUserSocialInfo(UserDTO user);

    // 소셜 ID로 사용자 조회
    UserDTO findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    // 추가 정보 업데이트 (프로필 완성용)
    void updateUserAdditionalInfo(UserDTO user);

    // 비밀번호 업데이트 (소셜 계정에 비밀번호 추가)
    void updateUserPassword(UserDTO user);

    // 사용자 목록 조회
    List<UserDTO> getUserList();

    // 사용자 권한 업데이트
    int updateUserRole(UserDTO user);
}