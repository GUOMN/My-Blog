package com.my.blog.website.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.AttachVoMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.utils.TaleUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 郭梦男
 * @date 2019/7/19 11:08
 */
@ConditionalOnProperty(value = "app.attach.impl", havingValue = "OSS")
@Service
public class OSSAttachServiceImpl extends AbstractAttachServiceImpl {

  final private String bucketName;
  final private OSS ossClient;
  final private String URL_PREFIX;

  @Autowired
  public OSSAttachServiceImpl(AttachVoMapper attachDao,
      @Value("${oss.endpoint}") String endpoint,
      @Value("${oss.accessKeyId}") String accessKeyId,
      @Value("${oss.accessKeySecret}") String accessKeySecret,
      @Value("${oss.bucketName}") String bucketName){
    super(attachDao);
    this.bucketName = bucketName;
    this.ossClient = initOssClient(endpoint, accessKeyId, accessKeySecret);
    URL_PREFIX = "https://" + bucketName + "." + endpoint.replaceFirst("http://", "") + "/";
  }

  private OSS initOssClient(String endpoint, String accessKeyId, String accessKeySecret) {
    return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
  }

  @PreDestroy
  void destroyOssClient(){
    if(null != ossClient){
      ossClient.shutdown();
    }
  }

  @Override
  public List<String> save(Integer uid, MultipartFile[] multipartFiles) {
    List<String> errorFiles = new ArrayList<>();
    try {
      for (MultipartFile multipartFile : multipartFiles) {
        String fname = multipartFile.getOriginalFilename();
        if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE) {
          assert fname != null;
          String fkey = TaleUtils.getFileKey(fname).replaceFirst("/", "");

          String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
          try {
            PutObjectResult result = upload(fkey, multipartFile.getBytes());
            LOG.info(String.valueOf(result));
          } catch (IOException e) {
            e.printStackTrace();
          }
          // 数据库存储时附加域名信息
          save2db(fname, URL_PREFIX + fkey, ftype, uid);
        } else {
          errorFiles.add(fname);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return errorFiles;
  }

  @Override
  @Transactional
  public void deleteById(Integer id) {
    if (null != id) {
      String fkey = selectById(id).getFkey();
      deleteFile(fkey.replace(URL_PREFIX, ""));
      attachDao.deleteByPrimaryKey(id);
    }
  }

  private PutObjectResult upload(String fkey, byte[] file){
    // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（fkey）。
    return ossClient.putObject(bucketName, fkey, new ByteArrayInputStream(file));
  }

  private void deleteFile(String fkey){
    // 删除文件。
    ossClient.deleteObject(bucketName, fkey);
  }

}
