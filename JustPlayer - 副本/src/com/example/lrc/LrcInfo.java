package com.example.lrc;

import java.util.Map;

/**
 * ������װ�����Ϣ����
 * @author Administrator
 *
 */
public class LrcInfo {
    private String title;//������
	private String singer;//�ݳ���
	private String album;//ר��	
	private Map<Long,String> infos;//��������Ϣ��ʱ���һһ��Ӧ��Map
   //����Ϊgetter()  setter()
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public Map<Long, String> getInfos() {
		return infos;
	}
	public void setInfos(Map<Long, String> infos) {
		this.infos = infos;
	}
	
}
