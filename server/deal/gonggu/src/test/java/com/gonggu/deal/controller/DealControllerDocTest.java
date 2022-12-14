package com.gonggu.deal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonggu.deal.domain.*;
import com.gonggu.deal.repository.*;
import com.gonggu.deal.request.DealCreate;
import com.gonggu.deal.request.DealEdit;
import com.gonggu.deal.request.DealJoin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;


import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.gonngu.com",uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
public class DealControllerDocTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DealRepository dealRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DealMemberRepository dealMemberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DealImageRepository dealImageRepository;
    @Autowired
    private DealKeywordRepository dealKeywordRepository;
    @Autowired
    private KeywordRepository keywordRepository;
    private User testUser;


    @BeforeEach
    void clean(){
        dealMemberRepository.deleteAll();
        dealRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        keywordRepository.deleteAll();
        testUser = User.builder()
                .nickname("???????????????")
                .email("test@test.com")
                .password("password")
                .roles(Collections.singletonList("ROLE_USER")).build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("????????? ????????????")
    void getDeal() throws Exception{
        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        LocalDateTime now  = LocalDateTime.of(2022,12,12,0,0,0);
        List<Deal> deals = IntStream.range(0,20)
                .mapToObj(i -> Deal.builder()
                        .title("??????" +i)
                        .category(category)
                        .content("??????")
                        .price(1000L)
                        .unitPrice(200L)
                        .totalCount(i)
                        .url("url/")
                        .expireTime(now)
                        .quantity(10)
                        .unitQuantity(2)
                        .nowCount(i/2)
                        .build()).collect(Collectors.toList());
        dealRepository.saveAll(deals);

        List<DealImage> images =  IntStream.range(0, 20)
                .mapToObj(i -> DealImage.builder()
                        .deal(deals.get(i))
                        .build()).collect(Collectors.toList());
        dealImageRepository.saveAll(images);

        Keyword keyword = Keyword.builder().word("?????????").build();
        keywordRepository.save(keyword);
        List<DealKeyword> keywords =  IntStream.range(0, 60)
                .mapToObj(i -> DealKeyword.builder()
                        .deal(deals.get(i%20))
                        .keyword(keyword)
                        .build()).collect(Collectors.toList());
        dealKeywordRepository.saveAll(keywords);


        this.mockMvc.perform(get("/deal?title=??????&category=????????????&minPrice=1000&maxPrice=2000&order=1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/search"
                        , pathParameters(
                                parameterWithName("title").description("????????? ??????").optional()
                                        .attributes(key("constraint").value("????????? ????????? ????????? ?????? ?????????. '??????'??????????????????.")),
                                parameterWithName("category").description("???????????? ????????????").optional()
                                        .attributes(key("constraint").value("'????????????'??????????????????.")),
                                parameterWithName("minPrice").description("?????? ??????").optional(),
                                parameterWithName("maxPrice").description("?????? ??????").optional(),
                                parameterWithName("order").description("????????? ??????").optional()
                                        .attributes(key("constraint").value("???????????? ?????????, 1 = ?????????, 2 = ?????? ?????? ?????? ???"))
                        )
                        , responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("[].category").description("?????? ????????????"),
                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("[].remainDate").description("?????? ??????"),
                                fieldWithPath("[].unitPrice").description("?????? ??????"),
                                fieldWithPath("[].quantity").description("?????? ??????"),
                                fieldWithPath("[].nowCount").description("?????? ?????? ??????"),
                                fieldWithPath("[].totalCount").description("??? ?????? ??????"),
                                fieldWithPath("[].image.fileName").description("????????? ??????"),
                                fieldWithPath("[].image.thumbnail").description("????????? ??????"),
                                fieldWithPath("[].deleted").description("????????????"),
                                fieldWithPath("[].expired").description("????????????")
                        )
                )
        );
    }

    @Test
    @DisplayName("????????? ????????????")
    void getDealDetail() throws Exception{
        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        User user = User.builder()
                .nickname("??????")
                .email("user@test.com")
                .password("password").build();
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .unit("??????")
                .totalCount(10)
                .url("url/")
                .createTime(now)
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal);

        List<DealImage> images = IntStream.range(0,3)
                .mapToObj(i-> DealImage.builder()
                        .deal(deal)
                        .fileName("?????????"+i)
                        .build()).collect(Collectors.toList());
        dealImageRepository.saveAll(images);

        Keyword keyword = Keyword.builder().word("?????????").build();
        keywordRepository.save(keyword);
        List<DealKeyword> keywords =  IntStream.range(0,3)
                .mapToObj(i -> DealKeyword.builder()
                        .deal(deal)
                        .keyword(keyword)
                        .build()).collect(Collectors.toList());
        dealKeywordRepository.saveAll(keywords);

        this.mockMvc.perform(get("/deal/{dealId}", deal.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/detail"
                        , pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                        , responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("remainDate").description("?????? ??????"),
                                fieldWithPath("price").description("??????"),
                                fieldWithPath("unitPrice").description("?????? ??????"),
                                fieldWithPath("quantity").description("?????? ??????"),
                                fieldWithPath("unitQuantity").description("?????? ??????"),
                                fieldWithPath("unit").description("??????"),
                                fieldWithPath("nowCount").description("?????? ?????? ??????"),
                                fieldWithPath("totalCount").description("??? ?????? ??????"),
                                fieldWithPath("url").description("?????? URL"),
                                fieldWithPath("view").description("?????????"),
                                fieldWithPath("images[].fileName").description("????????? ??????"),
                                fieldWithPath("images[].thumbnail").description("????????? ??????"),
                                fieldWithPath("deleted").description("?????? ??????"),
                                fieldWithPath("expired").description("?????? ??????"),
                                fieldWithPath("user").description("????????? ?????? ?????? ?????????"),
                                fieldWithPath("category.id").description("????????? ???????????? ID"),
                                fieldWithPath("category.name").description("????????? ???????????? ??????"),
                                fieldWithPath("keywords").description("????????? ?????????"),
                                fieldWithPath("expiredDate").description("????????? ?????????")
                        )
                )
        );
    }

    @Test
    @DisplayName("????????? ??????")
    @WithMockUser
    void postDeal() throws Exception{

        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);
        LocalDateTime now = LocalDateTime.now();
        List<String> keywords = IntStream.range(0,3).mapToObj(i -> "?????????"+i).collect(Collectors.toList());
        List<String> images = IntStream.range(0,3).mapToObj(i -> "?????????"+i).collect(Collectors.toList());
        DealCreate dealCreate = DealCreate.builder()
                .title("???????????????.")
                .content("???????????????.")
                .price(10000L)
                .unitQuantity(5)
                .unit("??????")
                .nowCount(1)
                .totalCount(5)
                .categoryId(category.getId())
                .url("url ??????")
                .keywords(keywords)
                .images(images)
                .expireTime(now.plusDays(2))
                .build();

        this.mockMvc.perform(post("/deal")
                        .content(objectMapper.writeValueAsString(dealCreate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/create"
                        , requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("price").description("?????? ??????"),
                                fieldWithPath("unitQuantity").description("?????? ?????? ??????"),
                                fieldWithPath("unit").description("?????? ??????"),
                                fieldWithPath("nowCount").description("?????? ?????? ??????(?????? ??? ??????)"),
                                fieldWithPath("totalCount").description("??? ?????? ??????(?????? ??? ??????)"),
                                fieldWithPath("url").description("?????? URL"),
                                fieldWithPath("categoryId").description("???????????? ID"),
                                fieldWithPath("keywords").description("?????????"),
                                fieldWithPath("images").description("?????????"),
                                fieldWithPath("expireTime").description("????????? ?????? ??????")
                        )
                ));
    }


    @Test
    @DisplayName("????????? ??????")
    @WithMockUser
    void editDeal() throws Exception{
        User user = User.builder()
                .nickname("??????")
                .email("user@test.com")
                .password("password").build();
        userRepository.save(user);

        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal);
        Deal deal2 = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal2);
        DealImage tempImage = DealImage.builder().deal(deal2).fileName("?????? ?????????").build();
        dealImageRepository.save(tempImage);

        List<DealImage> images = IntStream.range(0,3)
                .mapToObj(i-> DealImage.builder()
                        .deal(deal)
                        .fileName("?????????"+i)
                        .build()).collect(Collectors.toList());
        dealImageRepository.saveAll(images);

        Keyword keyword = Keyword.builder().word("?????????").build();
        keywordRepository.save(keyword);
        List<DealKeyword> keywords =  IntStream.range(0,3)
                .mapToObj(i -> DealKeyword.builder()
                        .deal(deal)
                        .keyword(keyword)
                        .build()).collect(Collectors.toList());
        dealKeywordRepository.saveAll(keywords);

        List<String> editKeywords = IntStream.range(0,3).mapToObj(i -> "???????????????"+i).collect(Collectors.toList());
        List<String> editImages = IntStream.range(0,3).mapToObj(i -> "???????????????"+i).collect(Collectors.toList());
        DealEdit dealEdit = DealEdit.builder()
                .content("????????????")
                .images(editImages)
                .keywords(editKeywords)
                .build();

        this.mockMvc.perform(patch("/deal/{dealId}",deal.getId())
                        .content(objectMapper.writeValueAsString(dealEdit))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/edit"
                        , pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                        , requestFields(
                                fieldWithPath("content").description("?????? ??????").optional(),
                                fieldWithPath("images").description("?????? ?????????").optional(),
                                fieldWithPath("keywords").description("?????? ?????????").optional()
                        )
                ));
        this.mockMvc.perform(get("/deal/{dealId}",deal.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("????????? ??????")
    @WithMockUser
    void deleteDeal() throws Exception{
        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(testUser)
                .build();
        dealRepository.save(deal);


        this.mockMvc.perform(delete("/deal/{dealId}",deal.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/delete"
                        , pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                ));
        this.mockMvc.perform(get("/deal/{dealId}",deal.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("?????? ??????")
    @WithUserDetails(value = "???????????????", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void requestJoin() throws Exception{


        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        User user = User.builder()
                .nickname("??????")
                .email("test@test.com")
                .password("password").build();
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal);
        DealMember dealMember = DealMember.builder()
                .deal(deal)
                .user(user)
                .quantity(2)
                .build();
        dealMemberRepository.save(dealMember);
        DealJoin dealJoin = DealJoin.builder()
                .quantity(5)
                .build();

        this.mockMvc.perform(post("/deal/{dealId}/enrollment", deal.getId())
                        .content(objectMapper.writeValueAsString(dealJoin))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/join"
                        ,pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                        , requestFields(
                                fieldWithPath("quantity").description("?????? ??????")
                        )
                ));
    }
    @Test
    @DisplayName("?????? ?????? ??????")
    @WithUserDetails(value = "???????????????", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editJoin() throws Exception{
        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        User user = User.builder()
                .nickname("??????")
                .email("user@test.com")
                .password("password").build();
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal);

        DealJoin dealJoin = DealJoin.builder()
                .quantity(5)
                .build();

        DealJoin dealJoin2 = DealJoin.builder()
                .quantity(2)
                .build();

        this.mockMvc.perform(post("/deal/{dealId}/enrollment", deal.getId())
                        .content(objectMapper.writeValueAsString(dealJoin))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(patch("/deal/{dealId}/enrollment", deal.getId())
                        .content(objectMapper.writeValueAsString(dealJoin2))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/join-edit"
                        ,pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                        , requestFields(
                                fieldWithPath("quantity").description("?????? ?????? ??????")
                        )
                ));

    }

    @Test
    @DisplayName("?????? ??????")
    @WithUserDetails(value = "???????????????", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteJoin() throws Exception{
        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);

        User user = User.builder()
                .nickname("??????")
                .email("user@test.com")
                .password("password").build();
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Deal deal = Deal.builder()
                .category(category)
                .title("??????")
                .content("??????")
                .price(1000L)
                .quantity(10)
                .unitQuantity(2)
                .unitPrice(200L)
                .totalCount(10)
                .url("url/")
                .expireTime(now.plusDays(3))
                .nowCount(2)
                .user(user)
                .build();
        dealRepository.save(deal);

        DealJoin dealJoin = DealJoin.builder()
                .quantity(5)
                .build();

        this.mockMvc.perform(post("/deal/{dealId}/enrollment", deal.getId())
                        .content(objectMapper.writeValueAsString(dealJoin))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/deal/{dealId}/enrollment", deal.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/join-delete"
                        ,pathParameters(
                                parameterWithName("dealId").description("????????? ID")
                        )
                ));
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void getMySellList() throws Exception{
        User user = User.builder()
                .nickname("??????")
                .email("user@test.com")
                .password("password").build();
        userRepository.save(user);

        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);
        LocalDateTime date = LocalDateTime.now();
        List<Deal> deals = IntStream.range(0,5)
                .mapToObj(i -> Deal.builder()
                        .title("??????" +i)
                        .category(category)
                        .content("??????")
                        .price(1000L)
                        .unitPrice(200L)
                        .totalCount(i)
                        .url("url/")
                        .expireTime(date.plusDays(i%4))
                        .quantity(10)
                        .unitQuantity(2)
                        .nowCount(i)
                        .unit("??????")
                        .user(user)
                        .build()).collect(Collectors.toList());
        dealRepository.saveAll(deals);
        List<DealImage> images =  IntStream.range(0, 5)
                .mapToObj(i -> DealImage.builder()
                        .deal(deals.get(i))
                        .fileName("?????????"+i)
                        .thumbnail(true)
                        .build()).collect(Collectors.toList());
        dealImageRepository.saveAll(images);

        Keyword keyword = Keyword.builder().word("?????????").build();
        keywordRepository.save(keyword);
        List<DealKeyword> keywords =  IntStream.range(0, 20)
                .mapToObj(i -> DealKeyword.builder()
                        .deal(deals.get(i%5))
                        .keyword(keyword)
                        .build()).collect(Collectors.toList());
        dealKeywordRepository.saveAll(keywords);

        List<DealMember> dealMembers = IntStream.range(0,5)
                .mapToObj(i-> DealMember.builder()
                        .deal(deals.get(i))
                        .quantity(i+1)
                        .user(user)
                        .host(true)
                        .build()).collect(Collectors.toList());
        dealMemberRepository.saveAll(dealMembers);

        this.mockMvc.perform(get("/deal/sale/{userId}",user.getNickname())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/sell-list"
                        , pathParameters(
                                parameterWithName("userId").description("?????? ID")
                        )
                        , responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("[].category").description("?????? ????????????"),
                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("[].remainDate").description("?????? ??????"),
                                fieldWithPath("[].unitPrice").description("?????? ??????"),
                                fieldWithPath("[].quantity").description("?????? ??????"),
                                fieldWithPath("[].nowCount").description("?????? ?????? ??????"),
                                fieldWithPath("[].totalCount").description("??? ?????? ??????"),
                                fieldWithPath("[].image.fileName").description("????????? ??????"),
                                fieldWithPath("[].image.thumbnail").description("????????? ??????"),
                                fieldWithPath("[].deleted").description("?????? ??????"),
                                fieldWithPath("[].expired").description("?????? ??????"),
                                fieldWithPath("[].userCount").description("?????? ?????? ??????"),
                                fieldWithPath("[].unit").description("?????? ???"),
                                fieldWithPath("[].expiredDate").description("?????? ??????"),
                                fieldWithPath("[].hostName").description("????????? ??????")
                        )
                ));

    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void getMyJoinListTemp() throws Exception{

        List<User> users = IntStream.range(0,5)
                .mapToObj(i -> User.builder()
                        .nickname("??????" +i)
                        .email("email.com")
                        .password("password")
                        .build()).collect(Collectors.toList());
        userRepository.saveAll(users);

        Category category = Category.builder()
                .name("????????????").build();
        categoryRepository.save(category);
        LocalDateTime date = LocalDateTime.now();
        List<Deal> deals = IntStream.range(0,5)
                .mapToObj(i -> Deal.builder()
                        .title("??????" +i)
                        .category(category)
                        .content("??????")
                        .price(1000L)
                        .unitPrice(200L)
                        .totalCount(i)
                        .url("url/")
                        .expireTime(date.plusDays(i%4))
                        .quantity(10)
                        .unitQuantity(2)
                        .nowCount(i)
                        .unit("??????")
                        .user(users.get(i))
                        .build()).collect(Collectors.toList());
        dealRepository.saveAll(deals);
        List<DealImage> images =  IntStream.range(0, 5)
                .mapToObj(i -> DealImage.builder()
                        .deal(deals.get(i))
                        .fileName("?????????"+i)
                        .thumbnail(true)
                        .build()).collect(Collectors.toList());
        dealImageRepository.saveAll(images);

        Keyword keyword = Keyword.builder().word("?????????").build();
        keywordRepository.save(keyword);
        List<DealKeyword> keywords =  IntStream.range(0, 20)
                .mapToObj(i -> DealKeyword.builder()
                        .deal(deals.get(i%5))
                        .keyword(keyword)
                        .build()).collect(Collectors.toList());
        dealKeywordRepository.saveAll(keywords);

        List<DealMember> dealMembers = IntStream.range(1,5)
                .mapToObj(i -> DealMember.builder()
                        .host(false)
                        .deal(deals.get(i%5))
                        .user(users.get(0))
                        .quantity(i%5)
                        .build()).collect(Collectors.toList());
        dealMemberRepository.saveAll(dealMembers);

        this.mockMvc.perform(get("/deal/enrollment/{userId}", users.get(0).getNickname())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("deal/join-list"
                        , pathParameters(
                                parameterWithName("userId").description("?????? ID")
                        )
                        , responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("[].category").description("?????? ????????????"),
                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("[].remainDate").description("?????? ??????"),
                                fieldWithPath("[].unitPrice").description("?????? ??????"),
                                fieldWithPath("[].quantity").description("?????? ??????"),
                                fieldWithPath("[].nowCount").description("?????? ?????? ??????"),
                                fieldWithPath("[].totalCount").description("??? ?????? ??????"),
                                fieldWithPath("[].image.fileName").description("????????? ??????"),
                                fieldWithPath("[].image.thumbnail").description("????????? ??????"),
                                fieldWithPath("[].deleted").description("?????? ??????"),
                                fieldWithPath("[].expired").description("?????? ??????"),
                                fieldWithPath("[].userCount").description("?????? ?????? ??????"),
                                fieldWithPath("[].unit").description("?????? ???"),
                                fieldWithPath("[].expiredDate").description("?????? ??????"),
                                fieldWithPath("[].hostName").description("????????? ??????")
                        )
                ));
    }
}
