# RecycleViewAdatper

## 概述
此项目包括三个内容

- HeaderRecycleAdapter,带头部的adapter
- RecycleScrollHelper,滑动检测辅助类
- StickHeaderItemDecoration,固定头部装饰类
- ExtraViewWrapAdapter,添加头部尾部装饰类

重要类源码在header模块中,app模块为相关的示例实用及引用.需要使用时可只导入header模块.

---

## 源码分析链接
- [StickHeaderItemDecoration--RecyclerView使用的固定头部装饰类](http://blog.csdn.net/u011374875/article/details/51744496)
- [RecycleViewScrollHelper--RecyclerView滑动事件检测的辅助类 ](http://blog.csdn.net/u011374875/article/details/51744448)
- [HeaderRecycleAdapter--通用的带头部RecycleView.Adapter ](http://blog.csdn.net/u011374875/article/details/51744332)
- [ExtraViewWrapperAdapter--添加额外头部尾部功能的装饰adapter](http://blog.csdn.net/u011374875/article/details/51882269)

---

## 使用示例
### HeaderRecycleAdapter
`HeaderRecycleAdapter`使用起来是比较简单的,只需要提供数据源,头部数据,还有自己实现数据绑定`IHeaderAdapterOption`接口即可.

- 实现`IHeaderAdapterOption`接口,自定义数据的绑定显示

```JAVA
//创建数据绑定option时可以设置泛型数据的类型,第一个为子item的数据类型,第二个为header的数据类型
private class HeaderAdapterOption implements HeaderRecycleAdapter.IHeaderAdapterOption<String, String> {
    //获取头部layout的类型
    @Override
    public int getHeaderViewType(int groupId, int position) {
       return -1;
    }

    //获取子item layout的类型
    @Override
    public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem, boolean isShowHeader) {
        if (isHeaderItem) {
            return getHeaderViewType(groupId, position);
        } else {
            return 0;
        }
    }

    //根据view的类型返回对应的layoutId,用于adapter加载界面
    @Override
    public int getLayoutId(int viewType) {
        switch (viewType) {
            case 0:
            case NO_HEADER_TYPE:
                return R.layout.item_content_2;
            case -1:
                return R.layout.item_header;
            default:
                return R.layout.item_content;
        }
    }

    //设置头部数据绑定
    @Override
    public void setHeaderHolder(int groupId, String header, HeaderRecycleViewHolder holder) {
        //注册rootView的监听事件
        holder.registerRootViewItemClickListener(MainActivity.this);
        //获取holder的缓存的子view进行数据绑定
        TextView tv_header = holder.getView(R.id.tv_header);
        if (tv_header != null) {
            tv_header.setText(header.toString());
        }
    }

    //子item数据绑定,类似头部数据绑定
    //参数提供了完整地子item的分组索引,分组内列表的索引,当前item所在的位置
    @Override
    public void setViewHolder(int groupId, int childId, int position, String itemData, HeaderRecycleViewHolder holder) {
        holder.registerRootViewItemClickListener(MainActivity.this);
        TextView tv_content = holder.getView(R.id.tv_content);
        tv_content.setText(itemData.toString());
    }
}
```

---

- 创建数据源及头部数据

```JAVA
mGroupList = new LinkedList<List<String>>();
mHeaderMap = new ArrayMap<Integer, String>();
int groupId = 0;
int count = 0;
count = groupId + 10;
//数据源使用 List<List> 的数据结构
for (; groupId < count; groupId++) {
    int childCount = 8;
    List<String> childList = new ArrayList<String>(childCount);
    for (int j = 0; j < childCount; j++) {
        childList.add("child - " + j);
    }
    mGroupList.add(childList);
    //头部数据使用与分组索引对应的 Map<Integer,xxx>
    mHeaderMap.put(groupId, "title - " + groupId);
}
```

---


- 创建`HeaderRecycleAdapter`并绑定到`RecycleView`

```JAVA
//创建 adapter
//参数要求包括 context,option,数据源及头部数据
HeaderRecycleAdapter adapter=new HeaderRecycleAdapter<String, String>(this, new HeaderAdapterOption(), mGroupList, mHeaderMap);
//绑定到recycleView
rv.setAdapter(adapter);
```

---

#### SimpleRecycleAdapter(简单版)

- `SimpleRecycleAdapter`是不带`header`的`Adapter`,其实跟普通的创建一个adapter的结果是没有什么区别的.
- `SimpleRecycleAdapter`的使用方式与`HeaderRecycleAdapter`是一致的,创建数据源及数据绑定option即可.通过以上的示例我们知道只有分组的情况下会显示`Header`,所以`SimpleRecycleAdapter`内部其实只是覆盖和修改了部分设置.
- 事实上,对一个可能需要显示头部某些情况下又不需要显示头部的情况来说,可以直接使用`HeaderRecycleAdapter`,通过设置adapter的属性`setIsShowHeader(boolean)`即可实现header的显示.`SimpleRecycleAdapter`更建议在仅普通显示不使用header的情况下使用(当然这种情况下完全可以不使用此adapter,使用普通的adapter即可.不过普通的adapter需要自己实现,使用simpleAdatper则不需要处理实现,仅需绑定数据了.)

---

- 修改数据源为一维数据`List<>`

```JAVA
//本质还是创建了一个List<List>的数据源,只是这里作了另一层转换,调用者可以不需要自己创建二维数据源,直接使用一维数据源即可
//一维数据源永远只放在第一项.并且不存在其它的一维数据源(这样就不会有不同的分组了)
public void setItemList(List<T> itemList) {
    List<List<T>> groupList = this.getGroupList();
    if (groupList == null) {
        groupList = new LinkedList<List<T>>();
    }
    groupList.clear();
    groupList.add(itemList);
    this.setGroupList(groupList);
    mItemList = itemList;
}
```

---

- 创建`SimpleRecycleAdapter`并绑定到`RecycleView`

```JAVA
//创建 adapter
//参数要求包括 context,option,数据源及头部数据
SimpleRecycleAdapter adapter=new SimpleRecycleAdapter<String>(this, new HeaderAdapterOption(), mItemList);
//绑定到recycleView
rv.setAdapter(adapter);
```

---

- 继承并完善`SimpleAdapterOption`

对于不显示header的情况来说,直接实现`IHeaderAdatperOption`需要实现的方法会很多,某些方法也可能会导致误解错乱.
所以提供了一个`SimpleAdapterOption`抽象类,实现了部分`IHeaderAdatperOption`方法,将需要实现的方法简化.建议在使用不带头部的adatper时继承此类实现相关的抽象方法会更好.

```JAVA
public class SimpleAdapterOption extends SimpleRecycleAdapter.SimpleAdapterOption<String> {
    @Override
    public int getViewType(int position) {
        return 0;
    }

    @Override
    public void setViewHolder(String itemData, int position, HeaderRecycleViewHolder holder) {
        //绑定数据
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_content;
    }
}
```

---

#### 示例图片
![](https://github.com/CrazyTaro/RecycleViewAdapter/raw/master/screenshot/normalHeaderRecycleAdapter.gif)

---

### RecycleScrollHelper
`RecycleScrollHelper`是一个辅助类,不需要进行额外的修改,所有相关的设置都可以通过方法进行进行设置.
由于`RecycleScrollHelper`是用于处理滑动事件的,所以需要实现其对应的回调接口,并作为参数传入.

---
- 实现接口

```JAVA
public class ScrollPositionChangedListener implements RecycleVIewScrollHelper.OnScrollPositionChangedListener{
    @Override
    public void onScrollToTop() {
        Log.i("scroll", "滑动到顶部");
    }

    @Override
    public void onScrollToBottom() {
        Log.i("scroll", "滑动到底部");
    }

    @Override
    public void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible) {
        //此时可能处于一个还在滑动的状态
        //滑动到顶部和低部事件只会在滑动状态停止时回调,处于滑动中是不会回调的
        Log.i("scroll", "滑动未达到底部或者顶部");
    }
}
```

---

- 创建辅助类实例并绑定到RecycleView

```JAVA
//参数传入OnScrollPositionChangedListener
RecycleVIewScrollHelper mScrollHelper = new RecycleVIewScrollHelper(this);
//设置是否在检测到某一种边界状态时(滑动到顶部或者底部),继续检测是否触发另一种状态,一般情况下很少用到
mScrollHelper.setCheckScrollToTopBottomTogether(false);
//设置是否优先检测顶部,若为false优先检测底部
mScrollHelper.setCheckScrollToTopFirstBottomAfter(false);
//设置检测底部时是否需要检测满屏状态(item填充满RecycleView的情况)
mScrollHelper.setCheckIfItemViewFullRecycleViewForBottom(true);
//设置检测顶部时是否需要检测满屏状态
mScrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
//设置检测顶部时的容差值,容差值越大,越容差触发事件
mScrollHelper.setTopOffsetFaultTolerance(100);
//设置检测底部时的容差值
mScrollHelper.setBottomFaultTolerance(100);
//关联相关的RecycleView,随时可以切换回调事件及绑定的RecylceView,此helper可以复用.
mScrollHelper.attachToRecycleView(mRvDisplay);
```

请注意,**scrollHelper是可以被复用的**,也就是说可以通过`helper.attachToRecycleView()`方法绑定到其它的RecycleView,但是同样存在的问题是,**一个scrollHelper只能绑定一个RecycleView,不能绑定多个**.

这是因为helper中回调接口时需要使用到绑定的recycleView,并且helper中只保存了一个recycleView.**重复绑定recyleView时会原有的会被替换.**

---

#### 示例图片
请参考HeaderRecyleAdatper的图片(**请注意弹出的toast**))

---

### StickItemDecoration
`StickItemDecoration`是用于显示固定头部的装饰类,使用它需要实现对应的接口(可以直接使用HeaderRecycleAdatper,该adapter已经实现了相关的接口).

- 实现`IStickerHeaderDecoration`
绘制固定头部时,`StickHeaderItemDecoration`完成固定头部的测量绘制,对于固定头部的获取/数据绑定等并不能进行处理,实际上该部分操作也不能由其处理,因此定义了一个接口,用于处理相关的固定头部的数据.
这个接口是`StickHeaderItemDecoration`所必需的,一般来说对于带头部的Adapter才需要考虑是否需要显示固定头部.所以`HeaderRecycleAdapter`已经默认实现了这个接口,不想自己实现的情况下,直接创建一个`HeaderRecycleAdapter`作为参数即可.

```JAVA
public interface IStickerHeaderDecoration {
    //判断当前位置的item是否为一个header
    public boolean isHeaderPosition(int position);

    //判断当前位置的item是否需要一个stick header view
    public boolean hasStickHeader(int position);

    //获取指定位置需要显示的headerView的标志,该标志用于缓存唯一的一个header类型的view.
    //不同的headerView应该使用不同的tag,否则会被替换
    public int getHeaderViewTag(int position, RecyclerView parent);

    //根据header标志或者position获取需要的headerView
    public View getHeaderView(int position, int headerViewTag, RecyclerView parent);

    //设置headerView显示的数据
    public void setHeaderView(int position, int headerViewTag, RecyclerView parent, View headerView);
    
    //判断当前渲染的header是否与上一次渲染的header为同一分组,若是可以不再测量与绑定数据
    //lastDecoratedPosition,上一次渲染stickHeader的位置
    //nowDecoratingPosition,当前需要渲染stickHeader的位置
    public boolean isBeenDecorated(int  lastDecoratedPosition, int nowDecoratingPosition);
}
```

---

- 绑定RecycleView

```JAVA
//创建固定头部装饰,参数为IStickerHeaderDecoration
StickHeaderItemDecoration mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
//设置recycleView与之绑定即可
mRvDisplay.addItemDecoration(mSickDecoration);

//使用 HeaderRecycleAdapter作为参数
//new StickHedaerItemDecoration(new HeaderRecycleAdapter(params...));

//也可以使用decoration的方法直接绑定recycleView
//注意两者不同之处在于,对于直接使用recycleView绑定的,一个decoration可以绑定多个recylceView,只要确保不会出错就行;
//如 mRv1.addItemDecoration(mSickDecoration); 
//mRv2.addItemDecoration(mSickDecoration);
//只要确定这个两个recycleView都可以使用这个decoration即可,同样的,切换adapter时decoration也是可以复用的.

//反之使用decoration的方法进行关联的,decoration永远只会关联一个,不会关联多个
//并且decoration会保留当前的recycleView的引用哦.
//mStickDecoration.attachToRecyclerView(mRvDisplay);
```

---

#### 示例图片
![](https://github.com/CrazyTaro/RecycleViewAdapter/raw/master/screenshot/normalItemDecorationSticker.gif)

---

### ExtraViewWrapperAdapter
可为`RecycleView`添加额外的headerView及footerView,同时也配置有refreshView及loadView两个独立的view添加(与headerView及footerView分开).

#### 使用方式
使用方式很简单,需要添加headerView或者footerView时,直接进行添加,然后设置原始数据innerAdatper,如果是实现了`ISpanSizeHandler`及`IStcikerHeaderDecoration`接口的adapter则不需要作额外处理,否则需要考虑一下是否要设置相关的接口实现.

```JAVA
//设置innerAdapter
mExtraAdapter = new ExtraViewWrapAdapter(mNormalAdapter);
//添加headerView
mExtraAdapter.addHeaderView(R.id.header_view, yourView);
//使用extraViewWrapperAdapter代码原有的adapter作为recycleView的数据绑定
rv.setAdapter(mExtraAdapter);
```

如果不需要添加headerView与footerView,需要添加一个刷新或者是加载的动画界面,可以使用以下方法

```JAVA
//设置刷新时显示的view
mExtraAdapter.setRefreshingHeaderView(yourView);
//使用静态方法设置当前为刷新状态,该方法会自动检测对应的rv(RecycleView)中的adapter是否为ExtraViewWrapperAdapter,然后自动对其进行设置并根据参数调整recycleView(如是否滑动到第一项,参数2)
ExtraViewWrapAdapter.setRefreshingViewStatus(true,true,rv);
```

加载使用的view与刷新的view是相同的使用方式.

---

## 示例图片
![图片](https://github.com/CrazyTaro/RecycleViewAdapter/raw/master/screenshot/extraViewWrapperAdapter.png)
