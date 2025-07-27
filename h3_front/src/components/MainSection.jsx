// ...existing code...
import React, { useState, useEffect } from 'react';
// import HeaderRightControls from './HeaderRightControls.jsx';
import axios from 'axios';
import {
  Card,
  CardBody,
  CardHeader,
  Typography,
  Button,
  Chip,
  Carousel,
} from "@material-tailwind/react";
import {
  HeartIcon,
  ExclamationTriangleIcon,
  HomeIcon,
  ShieldCheckIcon,
  PhoneIcon,        
  MapPinIcon,       
  ClockIcon, 
} from "@heroicons/react/24/outline";

import { useNavigate } from 'react-router-dom';
// ENV URL 임포트
import { API_SERVER_URL, WEBAPP_URL } from '@/config/env';

// MainSection 컴포넌트는 Drawer 상태를 상위(App)에서 prop으로 받음
const MainSection = (props) => {
  // props: isDrawerOpen, setIsDrawerOpen, drawerWidth (이제 App.jsx에서 관리)
  // Drawer는 App.jsx에서만 전역으로 관리, MainSection에서는 Drawer 관련 스타일만 적용
  const { isDrawerOpen = false, drawerWidth = 256 } = props;
  const navigate = useNavigate();
  // const [currentSlide, setCurrentSlide] = useState(0);
  // const [currentBannerSlide, setCurrentBannerSlide] = useState(0);
  const handleBannerClick = (banner) => {
    if (banner.link) {
      if (banner.isExternal) {
        // 새 탭에서 열기
        window.open(banner.link, '_blank');
      } else {
        // 내부 페이지 이동
        navigate(banner.link);
      }
    }
  };
  // 주요 서비스 블록 데이터 (관리자 전용 메뉴 필터 적용)
  const [userRole, setUserRole] = useState('');

  // 컴포넌트 마운트 시 사용자 역할 확인
  useEffect(() => {
    const checkUserRole = async () => {
      try {
        // auth 유틸리티 함수 사용 (Header와 동일한 방식)
        const { getUserRole, checkAuthStatus } = await import('@/utils/auth');
        const isValid = await checkAuthStatus();
        
        if (isValid) {
          const role = await getUserRole();
          console.log('[MainSection] 확인된 사용자 역할:', role);
          console.log('[MainSection] localStorage role:', localStorage.getItem('role'));
          setUserRole(role);
        } else {
          setUserRole('');
        }
      } catch (error) {
        console.error('[MainSection] 역할 확인 실패:', error);
        setUserRole('');
      }
    };

    checkUserRole();

    // localStorage 변경 감지 및 로그아웃 이벤트 처리
    const handleStorageChange = async (e) => {
      try {
        // 로그아웃 이벤트 감지
        if (e && e.key === 'logout') {
          setUserRole('');
          return;
        }
        
        const { getUserRole, checkAuthStatus } = await import('@/utils/auth');
        const isValid = await checkAuthStatus();
        
        if (isValid) {
          const role = await getUserRole();
          setUserRole(role);
        } else {
          setUserRole('');
        }
      } catch (error) {
        console.error('[MainSection] 역할 확인 실패:', error);
        setUserRole('');
      }
    };

    // 커스텀 로그아웃 이벤트 처리 (같은 탭에서 즉시 반영)
    const handleUserLogout = () => {
      console.log('[MainSection] 로그아웃 이벤트 감지');
      setUserRole('');
    };

    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('userLogout', handleUserLogout);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('userLogout', handleUserLogout);
    };
  }, []);
  const allMainServices = [
    {
      id: 1,
      title: "AI 보호견 검색",
      description: "사진을 업로드하면 AI가 비슷한 외모의 보호견을 찾아드려요",
      link: "/pet/search",
      icon: "/image/icon_mainServices_AI 보호견 검색.svg"
    },
    {
      id: 2,
      title: "AI 입양 추천",
      description: "앞으로 함께할 반려견을 AI가 추천해드려요",
      link: "/consult/chat",
      icon: "/image/icon_mainServices_AI 입양 추천.svg"
    },
    {
      id: 3,
      title: "AI 피부 검진",
      description: "강아지 피부 고민 AI 수의사에게 1차 상담 받아보세요",
      link: "/vet/skin",
      icon: "/image/icon_mainServices_AI 피부 검진.svg"
    },
    {
      id: 4,
      title: "AI 구조 가이드",
      description: "AI가 보호견 발견이 잦은 지역과 구조 가이드를 안내해드려요",
      link: "/search/heatmap",
      icon: "/image/icon_mainServices_AI 구조 가이드.svg"
    },
    {
      id: 5,
      title: "[관리자] AI 프로필 생성",
      description: "AI가 보호견 프로필을 자동으로 생성해드려요",
      link: "/admin/petmanagement",
      icon: "/image/icon_mainServices_AI 프로필 생성.svg",
      adminOnly: true // 관리자 전용 메뉴 표시
    }
  ];

  // 사용자 role에 따라 메뉴 필터링
  const mainServices = allMainServices.filter(service => {
    if (service.adminOnly) {
      return userRole === 'ADMIN'; // 관리자 전용 메뉴는 ADMIN 역할일 때만 표시
    }
    return true; // 일반 메뉴는 항상 표시
  });

  // 연락처 정보 추가
  const contactInfo = {
    phone: '1577-0954',
    systemInquiry: '054-810-8626',
    address: '39660 경상북도 김천시 혁신8로 177(율곡동)',
    workingHours: '평일 09:00 ~ 18:00'
  };

  // 메인 배너 이미지 데이터 (로컬 이미지 3개)
  const mainBanners = [
    {
      id: 1,
      image: '/image/banner01.jpg',
      alt: '배너1',
    },
    {
      id: 2,
      image: '/image/banner02.jpg',
      alt: '배너2',
      link: 'https://www.animal.go.kr/front/community/show.do?boardId=contents&seq=66&menuNo=2000000016',
      isExternal: true
    },
    {
      id: 3,
      image: '/image/banner03.jpg',
      alt: '배너3',
      link: '/consult/chat',
      isExternal: false
    },
  ];

  // 공지사항 목록 상태
  const [notices, setNotices] = useState([]);

  // 최근 보호견 발견신고 정보 (최대 2개)
  const [petFindInfos, setPetFindInfos] = useState([]);

  useEffect(() => {
    // 발견신고 정보 가져오기 (최대 5개만 사용) - 비회원도 접근 가능 (새로운 axios 인스턴스)
    const axiosInstance = axios.create({
      baseURL: API_SERVER_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    axiosInstance
      .get('/admin/notice/petfindinfo')
      .then((res) => {
        let data = Array.isArray(res.data) ? res.data : [];
        setPetFindInfos(data.slice(0, 5));
      })
      .catch((err) => {
        setPetFindInfos([]);
        console.error('petFindInfos fetch error', err);
      });
  }, []);

  // 공지사항 데이터 불러오기 (최근 3개, 실제 테이블 연동)
  useEffect(() => {
    // 공지사항 가져오기 - 비회원도 접근 가능 (새로운 axios 인스턴스)
    const axiosInstance = axios.create({
      baseURL: API_SERVER_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    axiosInstance
      .get('/admin/notice/select')
      .then((res) => {
        let data = Array.isArray(res.data) ? res.data : [];
        // 최신순 정렬 후 4개만
        const sorted = [...data].sort((a, b) => (b.id || 0) - (a.id || 0));
        setNotices(sorted.slice(0, 4));
      })
      .catch((err) => {
        setNotices([]);
        console.error('공지사항 fetch error', err);
      });
  }, []);

  // 보호견 목록(상세프로필 있는 랜덤 5개)
  const [abandonedDogs, setAbandonedDogs] = useState([]);
  const [dogActiveIndex, setDogActiveIndex] = useState(0); // 하단 dot UI용
  const [carouselActiveIndex, setCarouselActiveIndex] = useState(0); // Carousel 내부 인덱스


  useEffect(() => {
    // 랜덤 보호견 200마리 중 상세프로필 있는 보호견만 프론트에서 랜덤 5개 추출, 부족하면 없는 애들로 채움
    axios
      .post(`${API_SERVER_URL}/petcare/petlist/random`)
      .then((res) => {
        let dogs = Array.isArray(res.data) ? res.data : [];
        // 상세 프로필 있는 애들
        let withProfile = dogs.filter(dog => dog.profilehtml && dog.profilehtml.trim() !== '');
        // 상세 프로필 없는 애들
        let withoutProfile = dogs.filter(dog => !dog.profilehtml || dog.profilehtml.trim() === '');
        // 랜덤 5마리 추출 (상세 프로필 있는 애들 우선)
        let selected = [];
        if (withProfile.length >= 5) {
          selected = withProfile.sort(() => Math.random() - 0.5).slice(0, 5);
        } else {
          selected = [...withProfile.sort(() => Math.random() - 0.5)];
          const needed = 5 - selected.length;
          if (needed > 0) {
            selected = [
              ...selected,
              ...withoutProfile.sort(() => Math.random() - 0.5).slice(0, needed)
            ];
          }
        }
        // 필드명 변환: MainSection에서 쓸 수 있게 매핑
        const mapped = selected.map((dog) => ({
          imageUrl: dog.publicUrl || '',
          birth: dog.birthYyyyMm || '',
          gender: dog.genderCd || '',
          foundPlace: dog.foundLocation || '',
          shelterName: dog.shelterName || '',
          profileHtml: dog.profilehtml || '',
        }));
        setAbandonedDogs(mapped);
      })
      .catch((err) => {
        setAbandonedDogs([]);
        console.error('abandonedDogs fetch error', err);
      });
  }, []);


  const getBannerColor = (color) => {
    switch (color) {
      case 'blue':
        return 'from-blue-500 to-blue-600';
      case 'green':
        return 'from-green-500 to-green-600';
      case 'purple':
        return 'from-purple-500 to-purple-600';
      case 'orange':
        return 'from-orange-400 to-orange-500';
      default:
        return 'from-blue-500 to-blue-600';
    }
  };

  const carouselArrowStyle = `
  .carousel-arrow {
    transition: background 0.18s, color 0.18s, box-shadow 0.18s, transform 0.12s;
    border-radius: 9999px;
  }
  .carousel-arrow:hover {
    background: rgba(219, 234, 254, 0.3) !important; /* blue-100, 30% opacity */
    color: #2563eb !important;      /* blue-600 */
    box-shadow: 0 4px 16px rgba(37,99,235,0.18);
    /* transform: scale(1.13); */
    /* 세로 이동 없이 크기만 확대 */
    scale: 1.13;
  }
  .carousel-arrow:active {
    background: #bfdbfe !important; /* blue-200 */
    color: #1d4ed8 !important;      /* blue-700 */
    box-shadow: 0 2px 8px rgba(37,99,235,0.22);
    /* transform: scale(0.97); */
    scale: 0.97;
  }
  `;
  
  return (
    <>
      {/* Drawer/메인 flex-row 전체 래퍼 (Header는 상위(App)에서만 렌더링) */}
      <div className="flex flex-row w-full h-full relative transition-all duration-200">
        {/* 메인 컨텐츠: Drawer가 열리면 width가 줄어듦 */}
        <div
          id="main-content-block"
          className="transition-all duration-200"
          style={isDrawerOpen ? { width: `calc(100% - ${drawerWidth}px)`, minWidth: 0, filter: 'blur(0.5px)' } : { width: '100%', minWidth: 0 }}
        >
          <style>{carouselArrowStyle}</style>
          <main className="min-h-screen bg-white">
            {/* 0. 주요 서비스 타이틀 */}
            <section className="mb-0">
              <div className="container mx-auto px-4">
                <div className="w-full mb-2">
                  <Typography
                    variant="h4"
                    style={{
                      fontSize: '1.008rem',
                      color: '#1d4ed8',
                      fontWeight: 700,
                      paddingLeft: '1rem'
                    }}
                    className="mb-2"
                  >
                    주요 서비스
                  </Typography>
                </div>
                <div style={{ height: '0.5rem' }} />
                <div className={`grid grid-cols-1 md:grid-cols-3 gap-4 w-full ${mainServices.length === 4 ? 'lg:grid-cols-4' : 'lg:grid-cols-5'}`}>
                  {mainServices.map((service, idx) => (
                    <div
                      key={service.id}
                      className={`flex justify-center items-center w-full ${idx === 0 ? 'justify-start' : ''} ${idx === mainServices.length - 1 ? 'justify-end' : ''}`}
                    >
                      <Card
                        className="cursor-pointer bg-white border border-blue-gray-50 shadow-lg hover:shadow-[0_8px_32px_rgba(0,0,0,0.18)] transition-all duration-200 hover:scale-105 flex items-center justify-center"
                        style={{ width: '99%', height: '110%' }}
                        onClick={() => navigate(service.link)}
                      >
                        <CardBody 
                          className="text-center flex flex-col items-center h-full w-full relative"
                          style={{
                            height: '8.47rem',
                            minHeight: '8.47rem',
                            position: 'relative',
                            paddingTop: 0,
                            paddingBottom: 0,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            flex: 1
                          }}
                        >
                          <div
                            className="flex justify-center items-center w-full"
                            style={{
                              fontSize: '1.82rem',
                              position: 'absolute',
                              top: '33%',
                              left: 0,
                              width: '100%',
                              transform: 'translateY(-50%)',
                              zIndex: 2,
                              paddingBottom: '0.49rem'
                            }}
                          >
                            {service.icon.endsWith('.svg') ? (
                              <img
                                src={service.icon}
                                alt={service.title}
                                className="mx-auto"
                                style={{
                                  width: '4.55rem',
                                  height: '4.55rem'
                                }}
                              />
                            ) : (
                              <span>{service.icon}</span>
                            )}
                          </div>
                          <div style={{ marginTop: '5.11rem', width: '100%' }}>
                            <Typography
                              variant="h6"
                              className="font-bold text-gray-800"
                              style={{
                                fontSize: '0.91rem',
                                marginBottom: '0.245rem',
                                paddingTop: '0.21rem',
                                paddingBottom: '0.105rem'
                              }}
                            >
                              {service.title}
                            </Typography>
                            <Typography
                              variant="small"
                              className="text-gray-600 leading-tight"
                              style={{
                                fontSize: '0.728rem',
                                minHeight: '1.54rem',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                marginTop: '0.105rem',
                                paddingTop: '0.105rem'
                              }}
                            >
                              {service.description}
                            </Typography>
                          </div>
                        </CardBody>
                      </Card>
                    </div>
                  ))}
                </div>
              </div>
            </section>
            <div style={{ height: '2rem' }} />
            {/* 3. 공지사항/보호견 목록 */}
            <section>
              <div className="container mx-auto px-4">
                <div
                  className="grid grid-cols-1 lg:grid-cols-2 gap-4 w-full min-w-0"
                  style={{
                    maxWidth: '100%',
                    width: '100%',
                    minWidth: 0,
                    overflowX: 'visible',
                    overflowY: 'visible',
                    boxSizing: 'border-box',
                  }}
                >
                  {/* 공지사항 */}
                  <div
                    className="bg-white rounded-xl w-full h-full max-w-full flex flex-col"
                    style={{
                      overflow: 'visible',
                      position: 'relative',
                      zIndex: 1,
                      margin: '0 0.5rem 0.5rem 0',
                    }}
                  >
                    <Card className="bg-white rounded-xl flex flex-col shadow-lg border border-blue-gray-50 w-full h-full max-w-full" style={{height: '100%', overflow: 'visible'}}>
                    <CardHeader
                        floated={false}
                        shadow={false}
                        className="rounded-none flex items-center flex-shrink-0 px-4"
                        style={{
                          minHeight: '2.88rem',
                          height: '2.88rem',
                          paddingTop: 0,
                          paddingBottom: 0,
                          backgroundColor: '#fff',
                          width: '100%',
                          boxSizing: 'border-box',
                          borderTopLeftRadius: '0.75rem',
                          borderTopRightRadius: '0.75rem',
                          margin: 0
                        }}
                      >
                        <div className="flex items-center gap-2 h-full">
                          <ExclamationTriangleIcon
                            style={{
                              width: '1.05rem',
                              height: '1.05rem',
                              color: '#1d4ed8'
                            }}
                          />
                          <Typography
                            variant="h4"
                            style={{
                              fontSize: '1.008rem',
                              color: '#1d4ed8'
                            }}
                          >
                            공지사항
                          </Typography>
                        </div>
                        {/* 관리자 권한자만 + 버튼 표시 */}
                        {userRole === 'ADMIN' && (
                          <div className="ml-auto flex items-center">
                            <button
                              onClick={() => navigate('/admin/notice')}
                              className="rounded-full flex items-center justify-center transition-all duration-200"
                              style={{
                                width: '1.4rem',
                                height: '1.4rem',
                                fontSize: '1.05rem',
                                backgroundColor: 'transparent',
                                color: '#1d4ed8',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                marginLeft: 'auto',
                                marginRight: 0
                              }}
                              onMouseOver={e => { e.currentTarget.style.backgroundColor = '#eff6ff'; }}
                              onMouseOut={e => { e.currentTarget.style.backgroundColor = 'transparent'; }}
                            >
                              <span className="font-bold" style={{ fontSize: '1.05rem' }}>+</span>
                            </button>
                          </div>
                        )}
                      </CardHeader>
                      <CardBody className="p-4">
                        <div className="space-y-2 mt-2" style={{ backgroundColor: '#fff', borderRadius: '0.75rem', padding: '0.5rem 0.5rem' }}>
                          {notices.length === 0 ? (
                            <Typography variant="small" className="text-blue-gray-400 text-center">공지사항이 없습니다.</Typography>
                          ) : (
                            notices.map((notice) => (
                              <div key={notice.id} className="border-b border-blue-gray-100 pb-1.5 last:border-b-0">
                                <div className="flex flex-col min-h-0" style={{ minHeight: '1.28rem' }}>
                                  <Typography 
                                    variant="h6" 
                                    className="hover:text-blue-500 cursor-pointer font-bold mb-0.5 text-gray-800"
                                    style={{ fontSize: '0.91rem', lineHeight: '1.2rem', maxWidth: '100%' }}
                                  >
                                    {notice.title && notice.title.length > 25 ? notice.title.slice(0, 25) + '...' : notice.title}
                                  </Typography>
                                  <Typography 
                                    variant="small" 
                                    className="text-blue-gray-600" 
                                    style={{ fontSize: '0.728rem', lineHeight: '1.1rem', maxWidth: '100%' }}
                                  >
                                    {notice.content && notice.content.length > 25 ? notice.content.slice(0, 25) + '...' : notice.content}
                                  </Typography>
                                </div>
                              </div>
                            ))
                          )}
                        </div>
                      </CardBody>
                    </Card>
                  </div>
                  {/* 보호견 목록 */}
                  <div
                    className="bg-white rounded-xl w-full h-full max-w-full flex flex-col"
                    style={{
                      overflow: 'visible',
                      position: 'relative',
                      zIndex: 1,
                      margin: '0 0.5rem 0.5rem 0',
                    }}
                  >
                    <Card className="bg-white rounded-xl flex flex-col shadow-lg border border-blue-gray-50 w-full h-full max-w-full" style={{height: '100%', overflow: 'visible'}}>
                    <CardHeader
                        floated={false}
                        shadow={false}
                        className="rounded-none flex items-center flex-shrink-0 px-4"
                        style={{
                          minHeight: '2.88rem',
                          height: '2.88rem',
                          paddingTop: 0,
                          paddingBottom: 0,
                          backgroundColor: '#fff',
                          width: '100%',
                          boxSizing: 'border-box',
                          borderTopLeftRadius: '0.75rem',
                          borderTopRightRadius: '0.75rem',
                          margin: 0
                        }}
                      >
                        <div className="flex items-center gap-2 h-full w-full">
                          <HeartIcon
                            style={{
                              width: '1.05rem',
                              height: '1.05rem',
                              color: '#1d4ed8'
                            }}
                          />
                          <Typography
                            variant="h4"
                            style={{
                              fontSize: '1.008rem',
                              color: '#1d4ed8'
                            }}
                          >
                            보호견 목록
                          </Typography>
                          <div className="ml-auto flex items-center">
                            <button
                              onClick={() => navigate('/pet/list')}
                              className="rounded-full flex items-center justify-center transition-all duration-200"
                              style={{
                                width: '1.4rem',
                                height: '1.4rem',
                                fontSize: '1.05rem',
                                backgroundColor: 'transparent',
                                color: '#1d4ed8',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                marginLeft: 'auto',
                                marginRight: 0
                              }}
                              onMouseOver={e => { e.currentTarget.style.backgroundColor = '#eff6ff'; }}
                              onMouseOut={e => { e.currentTarget.style.backgroundColor = 'transparent'; }}
                            >
                              <span className="font-bold" style={{ fontSize: '1.05rem' }}>+</span>
                            </button>
                          </div>
                        </div>
                      </CardHeader>
                      <CardBody className="p-4">
                        <div
                          className="relative rounded-xl shadow-sm overflow-visible flex flex-col justify-between"
                          style={{ height: '13.44rem', backgroundColor: '#fff' }}
                        >
                          <div className="flex-1 flex flex-col justify-center">
                          <Carousel
                              className="rounded-none h-full"
                              autoplay={true}
                              loop={true}
                              prevArrow={({ handlePrev }) => (
                                <button
                                  onClick={handlePrev}
                                  className="carousel-arrow absolute top-1/2 -translate-y-1/2 z-10 text-blue-300 rounded-full p-2"
                                  style={{ left: 'calc(1.2rem - 28px)', background: 'transparent', boxShadow: 'none', border: 'none' }}
                                  aria-label="이전"
                                >
                                  <svg width="19" height="19" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M15 19l-7-7 7-7"/></svg>
                                </button>
                              )}
                              nextArrow={({ handleNext }) => (
                                <button
                                  onClick={handleNext}
                                  className="carousel-arrow absolute top-1/2 -translate-y-1/2 z-10 text-blue-300 rounded-full p-2"
                                  style={{ right: 'calc(1.2rem - 28px)', background: 'transparent', boxShadow: 'none', border: 'none' }}
                                  aria-label="다음"
                                >
                                  <svg width="19" height="19" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7"/></svg>
                                </button>
                              )}
                              navigation={({ setActiveIndex, activeIndex, length }) => (
                                // dot UI만 렌더링, setState 호출 금지 (React 경고 방지)
                                null
                              )}
                            >

                              {abandonedDogs.map((dog, idx) => (
                                <div
                                  key={dog.id || idx}
                                  className="relative h-full w-full bg-white flex items-center transition"
                                  style={{ paddingLeft: '1.1rem', paddingRight: '1.1rem' }}
                                >
                                  {/* 이미지: 왼쪽, 120% 더 크게 → 120% 추가 확대, 고정 */}
                                  <div className="flex-shrink-0 flex items-center justify-center" style={{ width: '8.4rem', height: '8.4rem', position: 'sticky', left: 0, zIndex: 2, background: 'white' }}>
                                    {dog.imageUrl ? (
                                      <img
                                        src={dog.imageUrl}
                                        alt={dog.name || '보호견'}
                                        className="rounded-lg object-cover"
                                        style={{ width: '8.82rem', height: '8.82rem', background: '#eee' }}
                                      />
                                    ) : (
                                      <div className="flex items-center justify-center bg-gray-200 rounded-lg text-6xl" style={{ width: '8.82rem', height: '8.82rem' }}>🐕</div>
                                    )}
                                  </div>
                                  {/* 정보: 오른쪽, 폰트 120% 확대, 정보만 스크롤 */}
                                  <div className="flex-1 flex flex-col justify-center pl-4" style={{ maxHeight: '8.4rem', overflow: 'hidden' }}>
                                    <div className="space-y-1" style={{ fontSize: '1.2em' }}>
                                      <div className="flex justify-between" style={{ fontSize: 'clamp(0.78rem, 1.1vw, 0.91rem)', lineHeight: '1.08rem', marginBottom: '0.18rem' }}>
                                        <span className="font-medium text-gray-600">출생년월:</span>
                                        <span className="text-gray-800">{(dog.birth && dog.birth.length > 25) ? dog.birth.slice(0, 25) + '...' : (dog.birth || '-')}</span>
                                      </div>
                                      <div className="flex justify-between" style={{ fontSize: 'clamp(0.78rem, 1.1vw, 0.91rem)', lineHeight: '1.08rem', marginBottom: '0.18rem' }}>
                                        <span className="font-medium text-gray-600">성별:</span>
                                        <span className="text-gray-800">{(dog.gender && ((dog.gender === 'M' ? '수컷' : dog.gender === 'F' ? '암컷' : '미상')).length > 25) ? (dog.gender === 'M' ? '수컷' : dog.gender === 'F' ? '암컷' : '미상').slice(0, 25) + '...' : (dog.gender === 'M' ? '수컷' : dog.gender === 'F' ? '암컷' : dog.gender ? '미상' : '-')}</span>
                                      </div>
                                      <div className="flex justify-between" style={{ fontSize: 'clamp(0.78rem, 1.1vw, 0.91rem)', lineHeight: '1.08rem', marginBottom: '0.18rem' }}>
                                        <span className="font-medium text-gray-600">발견장소:</span>
                                        <span className="text-gray-800">{(dog.foundPlace && dog.foundPlace.length > 25) ? dog.foundPlace.slice(0, 25) + '...' : (dog.foundPlace || '-')}</span>
                                      </div>
                                      <div className="flex justify-between" style={{ fontSize: 'clamp(0.78rem, 1.1vw, 0.91rem)', lineHeight: '1.08rem', marginBottom: '0.18rem' }}>
                                        <span className="font-medium text-gray-600">보호소:</span>
                                        <span className="text-gray-800">{(dog.shelterName && dog.shelterName.length > 25) ? dog.shelterName.slice(0, 25) + '...' : (dog.shelterName || '-')}</span>
                                      </div>
                                    </div>
                                    {/* 상세 프로필 버튼 */}
                                    <div className="flex justify-center mt-3">
                                      {dog.profileHtml && dog.profileHtml.trim() !== '' ? (
                                        <button
                                          type="button"
                                          className="rounded-full border border-blue-300 px-3 py-1 bg-white text-blue-400 font-semibold shadow-sm hover:bg-blue-50 transition"
                                          style={{ fontSize: '0.84rem' }}
                                          onClick={e => {
                                            e.stopPropagation();
                                            const newWindow = window.open('', '_blank', 'width=800,height=600');
                                            if (newWindow) {
                                              newWindow.document.open();
                                              newWindow.document.write(dog.profileHtml);
                                              newWindow.document.close();
                                            } else {
                                              alert('팝업 차단을 해제해주세요.');
                                            }
                                          }}
                                        >
                                          상세 프로필 확인
                                        </button>
                                      ) : (
                                        <button
                                          type="button"
                                          className="rounded-full border border-gray-300 px-3 py-1 bg-gray-100 text-gray-400 font-semibold shadow-sm cursor-not-allowed"
                                          style={{ fontSize: '0.84rem' }}
                                          disabled
                                        >
                                          상세 프로필 작성 필요
                                        </button>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              ))}
                            </Carousel>
                          </div>
                          {/* 슬라이드 현황 UI: 셀 하단에 고정 */}
                          <div className="flex justify-center items-center gap-2 mt-2 pb-1">
                            {abandonedDogs.length > 1 && abandonedDogs.map((_, i) => (
                              <span
                                key={i}
                                onClick={() => setDogActiveIndex(i)}
                                className={`block h-1.5 rounded-2xl transition-all cursor-pointer ${dogActiveIndex === i ? "w-6 bg-blue-300" : "w-2.5 bg-blue-100"}`}
                              />
                            ))}
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  </div>
                </div>
              </div>
            </section>
            <div style={{ height: '2rem' }} />
            {/* 5. 메인 배너/보호견 발견 신고 */}
            <section className="relative mb-8">
              <div className="container mx-auto px-4">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 w-full aspect-[3/1] min-w-0">
                  {/* 메인 배너 */}
                  <div className="lg:col-span-2">
                    <div className="w-full h-full min-w-0 max-w-full overflow-hidden">
                      <Carousel
                        className="rounded-none w-full h-full"
                        autoplay={true}
                        loop={true}
                        navigation={({ setActiveIndex, activeIndex, length }) => (
                          <div className="absolute bottom-4 left-2/4 z-50 flex -translate-x-2/4 gap-2">
                            {new Array(length).fill("").map((_, i) => (
                              <span
                                key={i}
                                className={`block h-2 cursor-pointer rounded-2xl transition-all content-[''] ${
                                  activeIndex === i ? "w-8 bg-white" : "w-4 bg-white/50"
                                }`}
                                onClick={() => setActiveIndex(i)}
                              />
                            ))}
                          </div>
                        )}
                      >
                        {mainBanners.map((banner) => (
                          <div
                            key={banner.id}
                            className={`relative w-full h-full flex items-center justify-start bg-gray-200 ${
                              banner.link ? 'cursor-pointer' : ''
                            }`}
                            style={{ width: '100%', height: '100%' }}
                            onClick={() => handleBannerClick(banner)}
                          >
                            <img
                              src={banner.image}
                              alt={banner.alt}
                              className="object-contain w-full h-full max-h-full max-w-full"
                              style={{ width: '100%', height: '100%' }}
                            />
                          </div>
                        ))}
                      </Carousel>
                    </div>
                  </div>
                  {/* 보호견 발견신고 */}
                  <div className="lg:col-span-1">
                    <div
                      className="bg-white rounded-xl flex flex-col shadow-lg border border-blue-gray-50 min-w-0 max-w-full overflow-hidden h-full"
                      style={{ width: '100%' }}
                    >
                      {/* 헤더 */}
                      <CardHeader
                        floated={false}
                        shadow={false}
                        className="rounded-none flex items-center justify-between"
                        style={{
                          minHeight: '2.4rem',
                          height: '2.4rem',
                          paddingTop: 0,
                          paddingBottom: 0,
                          backgroundColor: 'transparent'
                        }}
                      >
                        <div className="flex items-center gap-2 h-full">
                          <ExclamationTriangleIcon
                            style={{
                              width: '1.05rem',
                              height: '1.05rem',
                              color: '#1d4ed8'
                            }}
                          />
                          <span
                            style={{
                              fontSize: '1.008rem',
                              color: '#1d4ed8',
                              fontWeight: 700
                            }}
                          >
                            보호견 발견 신고
                          </span>
                        </div>
                        <button
                          className="rounded-full px-2.5 py-0.5 font-semibold text-blue-700 bg-blue-100 hover:bg-blue-200 transition-all duration-150"
                          style={{ fontSize: '0.91rem', transform: 'scale(0.9)' }}
                          onClick={() => navigate('/search/petsearchpage')}
                        >
                          바로가기
                        </button>
                      </CardHeader>
                      {/* 내용 영역: 최근 보호견 발견신고 정보 최대 4개 표시 */}
                      <div className="flex-1 flex flex-col items-start justify-center px-4 py-1 gap-3 w-full">
                        {petFindInfos.length === 0 ? (
                        <span className="text-blue-gray-400 text-sm">최근 발견 신고된 보호견 정보가 없습니다.</span>
                      ) : (
                        petFindInfos.slice(0,3).map((info, idx) => (
                          <div key={idx} className="w-full bg-white/80 rounded-lg border border-blue-gray-50 px-3 py-2 flex flex-col shadow-sm" style={{ position: 'relative', fontSize: '0.85rem', rowGap: '0.3rem' }}>
                            {/* 품종 (볼드, 1.1rem) */}
                            <div className="font-bold text-gray-800 mb-1" style={{ fontSize: '1.1em', lineHeight: '1.1em', maxWidth: '100%' }}>
                              {info.breed || '-'}
                            </div>
                            {/* 발견장소 (볼드, 1.1rem, 20자 제한) */}
                            <div className="font-bold text-gray-800 mb-1" style={{ fontSize: '1.1em', lineHeight: '1.1em', maxWidth: '100%' }}>
                              발견장소 : {(info.foundplace && info.foundplace.length > 20) ? info.foundplace.slice(0, 20) + '...' : (info.foundplace || '-')}
                            </div>
                            {/* 보호소, 발견날짜, 발견시간 모두 같은 줄 */}
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                              <div className="text-blue-gray-700" style={{ fontSize: '0.85em', lineHeight: '1.1em', maxWidth: '40%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                {(info.shelter && info.shelter.length > 12) ? info.shelter.slice(0, 12) + '...' : (info.shelter || '-')}
                              </div>
                              <div className="text-blue-gray-700" style={{ fontSize: '0.85em', lineHeight: '1.1em', textAlign: 'right', maxWidth: '60%', display: 'flex', justifyContent: 'flex-end', gap: '0.3rem', flexWrap: 'nowrap', alignItems: 'center' }}>
                                {(() => {
                                  if (!info.foundtime) return '발견 시간 : -';
                                  const dt = info.foundtime.replace('T', ' ').replace(/\..*$/, '');
                                  const match = dt.match(/(\d{4}-\d{2}-\d{2})[ T](\d{2}):(\d{2})/);
                                  if (match) {
                                    const date = match[1];
                                    const hour = match[2];
                                    return <span>발견 시간 : <span style={{ fontWeight: 500 }}>{date} {hour}시</span></span>;
                                  }
                                  return `발견 시간 : ${info.foundtime}`;
                                })()}
                              </div>
                            </div>
                          </div>
                        ))
                      )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </section>
            {/* 관련기관 - 정적 표시 */}
            <section className="bg-white py-8">
              <div className="container mx-auto px-4">
                <Typography variant="h4" className="text-center mb-6 text-blue-gray-800">
                  관련기관
                </Typography>
                <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
                  <Card className="shadow-lg hover:shadow-xl transition-shadow">
                    <CardBody className="text-center p-6">
                      <Typography variant="h6" className="mb-2 text-blue-gray-800">
                        국가동물보호정보시스템
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500 mb-4">
                        동물등록 및 보호정보
                      </Typography>
                      <Button 
                        size="sm" 
                        variant="filled"
                        className="bg-blue-500 hover:bg-blue-600"
                        onClick={() => window.open('https://www.animal.go.kr/', '_blank')}
                      >
                        방문하기
                      </Button>
                    </CardBody>
                  </Card>

                  <Card className="shadow-lg hover:shadow-xl transition-shadow">
                    <CardBody className="text-center p-6">
                      <Typography variant="h6" className="mb-2 text-blue-gray-800">
                        농림축산검역본부
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500 mb-4">
                        동물검역 및 질병관리
                      </Typography>
                      <Button 
                        size="sm" 
                        variant="filled"
                        className="bg-blue-500 hover:bg-blue-600"
                        onClick={() => window.open('https://www.qia.go.kr/', '_blank')}
                      >
                        방문하기
                      </Button>
                    </CardBody>
                  </Card>

                  <Card className="shadow-lg hover:shadow-xl transition-shadow">
                    <CardBody className="text-center p-6">
                      <Typography variant="h6" className="mb-2 text-blue-gray-800">
                        동물사랑배움터
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500 mb-4">
                        동물교육 플랫폼
                      </Typography>
                      <Button 
                        size="sm" 
                        variant="filled"
                        className="bg-blue-500 hover:bg-blue-600"
                        onClick={() => window.open('https://apms.epis.or.kr/', '_blank')}
                      >
                        방문하기
                      </Button>
                    </CardBody>
                  </Card>

                  <Card className="shadow-lg hover:shadow-xl transition-shadow">
                    <CardBody className="text-center p-6">
                      <Typography variant="h6" className="mb-2 text-blue-gray-800">
                        공공데이터포털
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500 mb-4">
                        공공데이터 개방
                      </Typography>
                      <Button 
                        size="sm" 
                        variant="filled"
                        className="bg-blue-500 hover:bg-blue-600"
                        onClick={() => window.open('https://www.data.go.kr/', '_blank')}
                      >
                        방문하기
                      </Button>
                    </CardBody>
                  </Card>

                  <Card className="shadow-lg hover:shadow-xl transition-shadow">
                    <CardBody className="text-center p-6">
                      <Typography variant="h6" className="mb-2 text-blue-gray-800">
                        한국유기동물복지협회
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500 mb-4">
                        유기동물 복지증진
                      </Typography>
                      <Button 
                        size="sm" 
                        variant="filled"
                        className="bg-blue-500 hover:bg-blue-600"
                        onClick={() => window.open('https://animalwa.org/', '_blank')}
                      >
                        방문하기
                      </Button>
                    </CardBody>
                  </Card>
                </div>
              </div>
            </section>
          </main>
        </div>
        {/* Drawer는 이제 App.jsx에서 전역으로 렌더링됨 */}
      </div>
      {/* 슬라이드 애니메이션 (Tailwind 커스텀) */}
      <style>{`
      @keyframes slideInDrawer {
        from { transform: translateX(100%); }
        to { transform: translateX(0); }
      }
      .animate-slideInDrawer {
        animation: slideInDrawer 0.22s cubic-bezier(0.4,0,0.2,1);
      }
      `}</style>
    </>
  );
}

// DrawerAccordionMenu는 별도 파일로 분리됨. 아래 export만 유지
export default MainSection;