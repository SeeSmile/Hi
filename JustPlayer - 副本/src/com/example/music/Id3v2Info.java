package com.example.music;

public class Id3v2Info {
	// ����
	private String tit2 = null;
	// ������
	private String tpe1 = null;
	// ר��
	private String talb = null;
	// ͷ��
	private byte[] apic = null;

	public Id3v2Info(String tit2, String tpe1, String talb, byte[] apic) {
		setTit2(tit2);
		setTpe1(tpe1);
		setTalb(talb);
		setApic(apic);

	}

	public void setTit2(String tit2) {
		this.tit2 = tit2;
	}

	public String getTit2() {
		return tit2;
	}

	public void setTpe1(String tpe1) {
		this.tpe1 = tpe1;
	}

	public String getTpe1() {
		return tpe1;
	}

	public void setTalb(String talb) {
		this.talb = talb;
	}

	public String getTalb() {
		return talb;
	}

	public void setApic(byte[] apic) {
		this.apic = apic;
	}

	public byte[] getApic() {
		return apic;
	}

}
