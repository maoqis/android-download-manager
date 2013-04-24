package com.yyxu.download.db;  
 /** 
  *����һ��������Ϣ��ʵ���� 
  */  
 public class DownloadInfo {  
	 int load_id;
	 String url;
	 int state;
	 long loadsize;
	 long totalsize;
	 
	public DownloadInfo() {
		super();
	}

	public DownloadInfo(int load_id, String url, int state, long loadsize,
			long totalsize) {
		super();
		this.load_id = load_id;
		this.url = url;
		this.state = state;
		this.loadsize = loadsize;
		this.totalsize = totalsize;
	}

	public int getLoad_id() {
		return load_id;
	}

	public void setLoad_id(int load_id) {
		this.load_id = load_id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getLoadsize() {
		return loadsize;
	}

	public void setLoadsize(long loadsize) {
		this.loadsize = loadsize;
	}

	public long getTotalsize() {
		return totalsize;
	}

	public void setTotalsize(long totalsize) {
		this.totalsize = totalsize;
	}

	@Override
	public String toString() {
		return "DownloadInfo [load_id=" + load_id + ", url=" + url + ", state="
				+ state + ", loadsize=" + loadsize + ", totalsize=" + totalsize
				+ "]";
	}
	 

 } 