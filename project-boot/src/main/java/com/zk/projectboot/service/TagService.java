package com.zk.projectboot.service;

import com.zk.projectboot.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TagService {

    Tag saveTag(Tag tag);

    Optional<Tag> getTagById(Integer id);

    Optional<Tag> getTagByName(String name);

    List<Tag> getAllTags();

    /**
     * 获取所有已批准的标签
     * @return 已批准标签列表
     */
    List<Tag> getAllApprovedTags();

    /**
     * 获取所有待审核的标签
     * @return 待审核标签列表
     */
    List<Tag> getPendingTags();

    /**
     * 审核标签 - 批准
     * @param id 标签ID
     * @return 操作是否成功
     */
    boolean approveTag(Integer id);

    /**
     * 审核标签 - 拒绝
     * @param id 标签ID
     * @return 操作是否成功
     */
    boolean rejectTag(Integer id);

    Page<Tag> searchTags(String keyword, Pageable pageable);

    /**
     * 搜索已批准的标签
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分页后的已批准标签列表
     */
    Page<Tag> searchApprovedTags(String keyword, Pageable pageable);

    List<Tag> getHotTags(int limit);

    List<Tag> getTagsByArticleId(Integer articleId);

    Tag updateTag(Tag tag);

    boolean deleteTag(Integer id);

    boolean existsByName(String name);
}
