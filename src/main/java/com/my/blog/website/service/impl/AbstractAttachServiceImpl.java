package com.my.blog.website.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.my.blog.website.dao.AttachVoMapper;
import com.my.blog.website.model.Vo.AttachVo;
import com.my.blog.website.model.Vo.AttachVoExample;
import com.my.blog.website.service.IAttachService;
import com.my.blog.website.utils.DateKit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangq on 2017/3/20.
 */
public abstract class AbstractAttachServiceImpl implements IAttachService {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractAttachServiceImpl.class);

    protected final AttachVoMapper attachDao;

    public AbstractAttachServiceImpl(AttachVoMapper attachDao) {
        this.attachDao = attachDao;
    }


    @Override
    public PageInfo<AttachVo> getAttachs(Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        AttachVoExample attachVoExample = new AttachVoExample();
        attachVoExample.setOrderByClause("id desc");
        List<AttachVo> attachVos = attachDao.selectByExample(attachVoExample);
        return new PageInfo<>(attachVos);
    }

    @Override
    public AttachVo selectById(Integer id) {
        if(null != id){
            return attachDao.selectByPrimaryKey(id);
        }
        return null;
    }

    void save2db(String fname, String fkey, String ftype, Integer author) {
        AttachVo attach = new AttachVo();
        attach.setFname(fname);
        attach.setAuthorId(author);
        attach.setFkey(fkey);
        attach.setFtype(ftype);
        attach.setCreated(DateKit.getCurrentUnixTime());
        attachDao.insertSelective(attach);
    }
}
