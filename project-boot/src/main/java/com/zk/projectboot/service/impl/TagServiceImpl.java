package com.zk.projectboot.service.impl;

import com.zk.projectboot.model.Tag;
import com.zk.projectboot.repository.TagRepository;
import com.zk.projectboot.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    public Optional<Tag> getTagById(Integer id) {
        return tagRepository.findById(id);
    }

    @Override
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * 获取所有已批准的标签
     * @return 已批准标签列表
     */
    @Override
    public List<Tag> getAllApprovedTags() {
        return tagRepository.findAll().stream()
                .filter(tag -> tag.getStatus() != null && tag.getStatus() == 1)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有待审核的标签
     * @return 待审核标签列表
     */
    @Override
    public List<Tag> getPendingTags() {
        return tagRepository.findAll().stream()
                .filter(tag -> tag.getStatus() != null && tag.getStatus() == 0)
                .collect(Collectors.toList());
    }

    /**
     * 审核标签 - 批准
     * @param id 标签ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean approveTag(Integer id) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        if (tagOptional.isPresent()) {
            Tag tag = tagOptional.get();
            tag.setStatus(1);  // 设置为已批准
            tagRepository.save(tag);
            return true;
        }
        return false;
    }

    /**
     * 审核标签 - 拒绝
     * @param id 标签ID
     * @return 操作是否成功
     */
    @Override
    @Transactional
    public boolean rejectTag(Integer id) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        if (tagOptional.isPresent()) {
            Tag tag = tagOptional.get();
            tag.setStatus(2);  // 设置为已拒绝
            tagRepository.save(tag);
            return true;
        }
        return false;
    }

    @Override
    public Page<Tag> searchTags(String keyword, Pageable pageable) {
        return tagRepository.findByNameContaining(keyword, pageable);
    }

    /**
     * 搜索已批准的标签
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分页后的已批准标签列表
     */
    @Override
    public Page<Tag> searchApprovedTags(String keyword, Pageable pageable) {
        Page<Tag> allTags = tagRepository.findByNameContaining(keyword, pageable);

        List<Tag> approvedTags = allTags.getContent().stream()
                .filter(tag -> tag.getStatus() != null && tag.getStatus() == 1)
                .collect(Collectors.toList());

        return new PageImpl<>(approvedTags, pageable, allTags.getTotalElements());
    }

    @Override
    public List<Tag> getHotTags(int limit) {
        return tagRepository.findHotTags(PageRequest.of(0, limit));
    }

    @Override
    public List<Tag> getTagsByArticleId(Integer articleId) {
        return tagRepository.findByArticleId(articleId);
    }

    @Override
    @Transactional
    public Tag updateTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public boolean deleteTag(Integer id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}
