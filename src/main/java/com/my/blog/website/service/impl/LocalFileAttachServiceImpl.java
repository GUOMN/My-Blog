package com.my.blog.website.service.impl;

import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.AttachVoMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.utils.TaleUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by wangq on 2017/3/20.
 */
@ConditionalOnProperty(value = "app.attach.impl", havingValue = "local")
@Service
public class LocalFileAttachServiceImpl extends AbstractAttachServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileAttachServiceImpl.class);

    @Autowired
    public LocalFileAttachServiceImpl(AttachVoMapper attachDao) {
        super(attachDao);
    }

    @Override
    @Transactional
    public List<String> save(Integer uid, MultipartFile[] multipartFiles) {
        List<String> errorFiles = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                String fname = multipartFile.getOriginalFilename();
                if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE) {
                    String fkey = TaleUtils.getFileKey(fname);
                    String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                    File file = new File(TaleUtils.getUploadFilePath() + fkey);
                    try {
                        FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    save2db(fname, fkey, ftype, uid);
                } else {
                    errorFiles.add(fname);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return errorFiles;

    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (null != id) {
            attachDao.deleteByPrimaryKey( id);
        }
    }
}
