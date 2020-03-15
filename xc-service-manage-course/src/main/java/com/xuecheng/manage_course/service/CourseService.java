package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.ext.CourseView;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;






    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    //获取课程根结点，如果没有则添加根结点
    public String getTeachplanRoot(String courseId){
        //校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }

        CourseBase courseBase = optional.get();

        //取出课程计划根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(teachplanList == null || teachplanList.size()==0){
            //新增一个结点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setPname(courseBase.getName());
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();

    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        //校验课程名称和课程计划名称
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }

        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出父节点id
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            //如果父结点为空则获取根结点
            parentid = getTeachplanRoot(courseid);
        }
        //取出父结点信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        //父节点
        Teachplan teachplanParent = teachplanOptional.get();
        //获取父节点级别
        String parentGrade = teachplanParent.getGrade();
        //设置父节点
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");
        //子节点的级别，根据父节点来判断
        if(parentGrade.equals("1")){
            teachplan.setGrade("1");
        }else if(parentGrade.equals("2")){
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public QueryResponseResult<CourseInfo> findCourseList( int page , int size, CourseListRequest courseListRequest){
        if (page<0){
            //设置页码
            page = 1;
        }

        if (size < 0){

            //设置页面显示个数
            size = 10;
        }

        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }

        //将companyId传给dao
        //设置分页参数
        PageHelper.startPage(page,size);

        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);

        //查询列表
        List<CourseInfo> list = courseListPage.getResult();

        //总记录数
        long total = courseListPage.getTotal();

        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setList(list);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    //添加课程
    public ResponseResult addCourse(CourseBase courseBase){
        if (courseBase == null) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        courseBase.setStatus("202001");
        //保存页面信息
        CourseBase save = courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程
    public CourseBase findCourseById(String id){
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CourseBase courseBase = optional.get();
        return courseBase;
    }

    //修改课程
    public ResponseResult updateCourse(String id,CourseBase courseBase){
        CourseBase one = this.findCourseById(id);

        one.setName(courseBase.getName());
        one.setUsers(courseBase.getUsers());
        one.setMt(courseBase.getMt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setTeachmode(courseBase.getTeachmode());
        one.setDescription(courseBase.getDescription());
        one.setSt(courseBase.getSt());
        one.setStatus(courseBase.getStatus());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //获取营销信息
    public CourseMarket getCourseMarket(String courseId){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }

        return null;
    }

    //更改营销信息
    public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket){
        CourseMarket one = this.getCourseMarket(id);
        if (one != null){
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());
            one.setEndTime(courseMarket.getEndTime());
            one.setValid(courseMarket.getValid());
            one.setQq(courseMarket.getQq());
            one.setPrice(courseMarket.getPrice());
            Float price = courseMarket.getPrice();
            one.setPrice_old(price+400);
            courseMarketRepository.save(one);
        }else{
            CourseMarket cm = new CourseMarket();
            cm.setId(id);
            cm.setCharge(courseMarket.getCharge());
            cm.setStartTime(courseMarket.getStartTime());
            cm.setEndTime(courseMarket.getEndTime());
            cm.setValid(courseMarket.getValid());
            cm.setQq(courseMarket.getQq());
            cm.setPrice(courseMarket.getPrice());
            Float price = courseMarket.getPrice();
            cm.setPrice_old(price+400);
            courseMarketRepository.save(cm);
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //添加课程图片
    @Transactional
    public ResponseResult svaeCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (picOptional.isPresent()) {
            coursePic = picOptional.get();
        }

        //没有课程图片
        if(coursePic == null ){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursepic(String courseId){

        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        return optional.get();
    }

    //删除图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){

        //执行删除，返回1表示删除成功
        long a = coursePicRepository.deleteByCourseid(courseId);
        if (a>0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }

    //课程视图查寻
    public CourseView getCourseView(String id){
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }

        //查询课程营销信息
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(id);
        if (courseMarket.isPresent()) {
            CourseMarket courseMarket1 = courseMarket.get();
            courseView.setCourseMarket(courseMarket1);
        }

        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()) {
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(coursePic);
        }

        //查看课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String couresId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(couresId);

        if (baseOptional.isPresent()) {
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    //课程预览
    public CoursePublishResult preview(String courseId){
        CourseBase one = this.findCourseBaseById(courseId);

        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面储存路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String id){
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);

        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程的发布装太
        CourseBase courseBase = this.saveCoursePubState(id);
        if (courseBase == null) {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //创建课程索引信息
        CoursePub coursePub = createCoursePub(id);
        //向数据库保存课程索引信息
        CoursePub newcoursePub = saveCoursePub(id,coursePub);
        if (newcoursePub == null) {
            //创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }
        //保存课程计划媒体资信息待索引表
        saveTeachplanMediaPub(id);

        //得到页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //保存CoursePub
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        if (StringUtils.isEmpty(id)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()) {
            coursePubNew = coursePubOptional.get();
        }

        if (coursePubNew == null){
            coursePubNew = new CoursePub();
        }

        BeanUtils.copyProperties(coursePub,coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //更新时间错为最新时间
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String data = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(data);

        coursePubNew.setValid("");
        coursePubRepository.save(coursePubNew);
        return coursePubNew;

    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()) {
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查看课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()) {
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String toJSONString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(toJSONString);
        return coursePub;
    }

    public CourseBase saveCoursePubState(String id) {
        CourseBase courseBaseById = this.findCourseBaseById(id);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    //保存媒体信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia){
        if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }

        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }

        Teachplan teachplan = optional.get();
        String grade = teachplan.getGrade();
        //只允许为叶子节点课程选择视频
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        TeachplanMedia one = null;
        Optional<TeachplanMedia> teachplanMediaOptional=teachplanMediaRepository.findById(teachplanId);
        if (!teachplanMediaOptional.isPresent()) {
            one = new TeachplanMedia();
        }else{
            one = teachplanMediaOptional.get();
        }
        //保存媒体信息与课程计划信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //保存课程计划媒体信息
    private void saveTeachplanMediaPub(String couseId){
        //查询课程媒体信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(couseId);
        //将课程计划媒体信息储存待索引
        teachplanMediaPubRepository.deleteByCourseId(couseId);
        ArrayList<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        for(TeachplanMedia teachplanMedia :teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);


    }

    //查询我的课程
    public QueryResponseResult<CourseInfo> myfindCourseList(String company_id, int page, int size, CourseListRequest courseListRequest){
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }

        //将公司id传入dao
        courseListRequest.setCompanyId(company_id);
        //分页
        PageHelper.startPage(page, size);
        //调用dao
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> list = courseListPage.getResult();
        long total = courseListPage.getTotal();
        QueryResult<CourseInfo> courseIncfoQueryResult = new QueryResult<CourseInfo>();
        courseIncfoQueryResult.setList(list);
        courseIncfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseIncfoQueryResult);
    }
}
