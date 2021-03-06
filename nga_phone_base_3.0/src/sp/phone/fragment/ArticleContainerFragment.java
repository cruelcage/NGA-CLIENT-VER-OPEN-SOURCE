package sp.phone.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import gov.anzong.androidnga.activity.PostActivity;
import gov.anzong.androidnga.R;
import sp.phone.adapter.ThreadFragmentAdapter;
import sp.phone.bean.PerferenceConstant;
import sp.phone.bean.ThreadData;
import sp.phone.interfaces.OnChildFragmentRemovedListener;
import sp.phone.interfaces.OnThreadPageLoadFinishedListener;
import sp.phone.interfaces.PagerOwnner;
import sp.phone.task.BookmarkTask;
import sp.phone.utils.ActivityUtil;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.StringUtil;
import sp.phone.utils.ThemeManager;

public class ArticleContainerFragment extends Fragment 
implements OnThreadPageLoadFinishedListener,PerferenceConstant,
PagerOwnner{
	public static ArticleContainerFragment create(int tid, int pid, int authorid){
		ArticleContainerFragment f = new ArticleContainerFragment();
		Bundle args = new Bundle ();
		args.putInt("tid", tid);
		args.putInt("pid", pid);
		args.putInt("authorid", authorid);
		f.setArguments(args);
		return f;
	}
	
	public ArticleContainerFragment() {
		super();
	}
	
	//TabHost tabhost;
	ViewPager  mViewPager;
	ThreadFragmentAdapter mTabsAdapter;
    int tid;
    int pid;
    int authorid;
	private static final String TAG= "ArticleContainerFragment";
	private static final String GOTO_TAG = "goto";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v  = inflater.inflate(R.layout.article_viewpager, container,false);


		/*PullToRefreshViewPager
		refreshPager = (PullToRefreshViewPager) v.findViewById(R.id.pull_refresh_viewpager);
		//refreshPager.setMode(Mode.DISABLED);
		mViewPager = refreshPager.getRefreshableView();*/
		mViewPager = (ViewPager) v.findViewById(R.id.pager);

		int pageFromUrl = 0;
		String url = getArguments().getString("url");
		if(null != url){
			tid = this.getUrlParameter(url, "tid");		
			pid = this.getUrlParameter(url, "pid");		
			authorid = this.getUrlParameter(url, "authorid");
			pageFromUrl = this.getUrlParameter(url, "page");
		}else
		{
			tid = this.getArguments().getInt("tid", 0);		
			pid = this.getArguments().getInt("pid", 0);
			authorid = this.getArguments().getInt("authorid", 0);
		}

		

		mTabsAdapter = new ThreadFragmentAdapter(getActivity(),
				getChildFragmentManager(),
				mViewPager,ArticleListFragment.class);
				//new TabsAdapter(getActivity(), tabhost, mViewPager,ArticleListFragment.class);
		
		mTabsAdapter.setArgument("id", tid);
		mTabsAdapter.setArgument("pid", pid);
		mTabsAdapter.setArgument("authorid", authorid);
		
		//ActivityUtil.getInstance().noticeSaying(getActivity());
		
        if (savedInstanceState != null) {
        	int pageCount = savedInstanceState.getInt("pageCount");
        	if(pageCount!=0)
        	{
        		mTabsAdapter.setCount(pageCount);
        		mViewPager.setCurrentItem(savedInstanceState.getInt("tab"));
        	}
        	
        }
        else if(pageFromUrl !=0)
        {
        	mTabsAdapter.setCount(pageFromUrl+1);
        	mViewPager.setCurrentItem(pageFromUrl);
        }
        else{
        	mTabsAdapter.setCount(1);
        }
		
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pageCount",mTabsAdapter.getCount());
        outState.putInt("tab",mViewPager.getCurrentItem());
	}
	
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		if(PhoneConfiguration.getInstance().HandSide==1){//lefthand
			int flag = PhoneConfiguration.getInstance().getUiFlag();
			if(flag==1 || flag==3||flag==5||flag==7){//文章列表，UIFLAG为1或者1+2或者1+4或者1+2+4
				inflater.inflate(R.menu.articlelist_menu_left, menu);
				}
			else{
				inflater.inflate(R.menu.articlelist_menu, menu);
			}
		}else{
			inflater.inflate(R.menu.articlelist_menu, menu);
		}


		MenuItem lock = menu.findItem(R.id.article_menuitem_lock);
		int orentation = ThemeManager.getInstance().screenOrentation;
		if(orentation ==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE||
				orentation ==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		{
			lock.setTitle(R.string.unlock_orientation);
			lock.setIcon(R.drawable.ic_menu_always_landscape_portrait);
			
		}
		
	}
	
	private int getUrlParameter(String url, String paraName){
		if(StringUtil.isEmpty(url))
		{
			return 0;
		}
		final String pattern = paraName+"=" ;
		int start = url.indexOf(pattern);
		if(start == -1)
			return 0;
		start +=pattern.length();
		int end = url.indexOf("&",start);
		if(end == -1)
			end = url.length();
		String value = url.substring(start,end);
		int ret = 0;
		try{
			ret = Integer.parseInt(value);
		}catch(Exception e){
			Log.e(TAG, "invalid url:" + url);
		}
		
		return ret;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId())
		{
			case R.id.article_menuitem_reply:
				//if(articleAdpater.getData() == null)
				//	return false;
				String tid = String.valueOf(this.tid);
				Intent intent = new Intent();
				intent.putExtra("prefix", "" );
				intent.putExtra("tid", tid);
				intent.putExtra("action", "reply");
				
				intent.setClass(getActivity(), PhoneConfiguration.getInstance().postActivityClass);
				startActivity(intent);
				if(PhoneConfiguration.getInstance().showAnimation)
					getActivity().overridePendingTransition(R.anim.zoom_enter,
							R.anim.zoom_exit);
				break;
			case R.id.article_menuitem_refresh:
				int current = mViewPager.getCurrentItem();
				ActivityUtil.getInstance().noticeSaying(getActivity());
				mViewPager.setAdapter(mTabsAdapter);
				mViewPager.setCurrentItem(current);
				
				
				break;
			case R.id.article_menuitem_addbookmark:				
				BookmarkTask bt = new BookmarkTask(getActivity());
				bt.execute(String.valueOf(this.tid));
				break;
			case R.id.article_menuitem_lock:
				
				handleLockOrientation(item);
				break;
			case R.id.goto_floor:
				createGotoDialog();
				break;
			case R.id.article_menuitem_back:
			default:
				getActivity().getSupportFragmentManager()
					.beginTransaction().remove(this).commit();
				OnChildFragmentRemovedListener father = null;
        		try{
        			 father = (OnChildFragmentRemovedListener) getActivity();
        			 father.OnChildFragmentRemoved(getId());
        		}catch(ClassCastException e){
        			Log.e(TAG,"father activity does not implements interface " 
        					+ OnChildFragmentRemovedListener.class.getName());
        			
        		}
				break;
		}
		return true;
	}
	
	private ImageButton getActionItem(int id){
		//View actionbar_compat = getActivity().findViewById(R.id.actionbar_compat);
		View ret = null;
		/*if(actionbar_compat != null)
		{
			ret = actionbar_compat.findViewById(id);
		}*/
		return (ImageButton) ret;
	}
	
	private void handleLockOrientation(MenuItem item){
		int preOrentation = ThemeManager.getInstance().screenOrentation;
		int newOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		ImageButton compat_item = null;//getActionItem(R.id.actionbar_compat_item_lock);
		
		if(preOrentation ==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE||
				preOrentation ==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			//restore
			//int newOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
			ThemeManager.getInstance().screenOrentation = newOrientation;
			
			getActivity().setRequestedOrientation(newOrientation);
			item.setTitle(R.string.lock_orientation);
			item.setIcon(R.drawable.ic_lock_screen);
			if(compat_item !=null)
				compat_item.setImageResource(R.drawable.ic_lock_screen);
			
		}else{
			newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			Display dis = getActivity().getWindowManager().getDefaultDisplay();
			//Point p = new Point();
			//dis.getSize(p);
			if(dis.getWidth() < dis.getHeight()){
				newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
			
			ThemeManager.getInstance().screenOrentation = newOrientation;
			getActivity().setRequestedOrientation(newOrientation);			
			item.setTitle(R.string.unlock_orientation);
			item.setIcon(R.drawable.ic_menu_always_landscape_portrait);
			if(compat_item !=null)
				compat_item.setImageResource(R.drawable.ic_menu_always_landscape_portrait);
		}
		
		
		
		SharedPreferences share = getActivity().getSharedPreferences(PERFERENCE,
				Activity.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putInt(SCREEN_ORENTATION, newOrientation);
		editor.commit();
		
	}

	@Override
	public void finishLoad(ThreadData data) {
		int exactCount = 1 + data.getThreadInfo().getReplies()/20;
		if(mTabsAdapter.getCount() != exactCount
				&&this.authorid == 0){
			mTabsAdapter.setCount(exactCount);
		}
		if(this.authorid>0){
			exactCount = 1 + data.get__ROWS()/20;
			mTabsAdapter.setCount(exactCount);
		}
		if( tid != data.getThreadInfo().getTid()) // mirror thread
			tid = data.getThreadInfo().getTid();
		
		
	}
	
	private void createGotoDialog(){

		int count = mTabsAdapter.getCount();
		Bundle args = new Bundle();
		args.putInt("count", count);
		
		DialogFragment df = new GotoDialogFragment();
		df.setArguments(args);
		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		
		Fragment prev = fm.findFragmentByTag(GOTO_TAG);
		if(prev != null){
			fm.beginTransaction().remove(prev).commit();
		}
		df.show(fm, GOTO_TAG);
		
	}

	@Override
	public int getCurrentPage() {
		return mViewPager.getCurrentItem()+1;
		
	}

	@Override
	public void setCurrentItem(int index) {
		mViewPager.setCurrentItem(index);
		
	}

	
	
}
