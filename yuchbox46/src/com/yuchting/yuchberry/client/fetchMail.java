/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;

class MailAttachment{
	int 		m_size;
	String 		m_name;
	String		m_type;
}

public class  fetchMail{
	
	final static int	VERSION = 4;
	
	public final static String	fsm_noSubjectTile = "No Subject";
	    	
	public final static int	ANSWERED 	= 1 << 0;
	public final static int	DELETED 	= 1 << 1;
	public final static int	DRAFT 		= 1 << 2;
	public final static int	FLAGGED 	= 1 << 3;
	public final static int	RECENT 		= 1 << 4;
	public final static int	SEEN 		= 1 << 5;
	
	public final static int	NOTHING_STYLE = 0;
	public final static int	FORWORD_STYLE = 1;
	public final static int	REPLY_STYLE = 2;
	
	private int 		m_mailIndex = 0;
	
	private Vector		m_vectFrom 		= new Vector();
	private Vector		m_vectReplyTo	= new Vector();
	private Vector		m_vectCCTo		= new Vector();
	private Vector		m_vectBCCTo		= new Vector();
	private Vector		m_vectTo		= new Vector();
	private Vector		m_vectGroup		= new Vector();
	
	private String			m_subject 		= "";
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= "";
	
	private String			m_contain		= "";
	private String			m_contain_html	= "";
	private String			m_contain_html_type	= "";
			
	private Vector	m_vectAttachment	 	= new Vector();
	
	private Message m_attachMessage		= null;
	
	private String			m_ownAccount	= "";
	
	private String			m_message_id 	= "";
	private String			m_in_reply_to	= "";
	private String			m_reference_id	= "";
	
	
	// location information
	boolean m_hasLocationInfo		= false;
	GPSInfo	m_gpsInfo 				= new GPSInfo();			
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public int GetSimpleHashCode(){
		return (GetSubject() + GetSendDate().getTime()).hashCode();
	}
	
	public void SetAttchMessage(Message m){ m_attachMessage = m;}
	public Message GetAttachMessage(){return m_attachMessage;}
		
	public static Address[] parseAddressList(Vector _list)throws Exception{
		Address[] 	t_addressList = new Address[_list.size()];
		
		for(int i = 0;i < _list.size();i++){
			String fullAdd = (String)_list.elementAt(i);
			String add;
			String t_name = null;
			
			final int t_start = fullAdd.indexOf('<');
			final int t_end = fullAdd.indexOf('>');
			
			final int t_start_quotation = fullAdd.indexOf('"');
			final int t_end_quotation = fullAdd.indexOf('"',t_start_quotation + 1);
			
			if(t_start_quotation != -1 && t_end_quotation != -1 ){			
				t_name = fullAdd.substring(t_start_quotation + 1, t_end_quotation);
			}else{
				if(t_start != -1 && t_start > 0){
					t_name = fullAdd.substring(0,t_start);
				}else{
					t_name = "";
				}				
			}
			
			if(t_start != -1 && t_end != -1 ){			
				add = fullAdd.substring(t_start + 1, t_end);
			}else{
				add = fullAdd;
			}
			
			t_addressList[i] = new Address(add,t_name);
		}
		
		return t_addressList;
	}
	
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		sendReceive.WriteInt(_stream,GetMailIndex());
		
		sendReceive.WriteStringVector(_stream,m_vectFrom);
		sendReceive.WriteStringVector(_stream,m_vectReplyTo);
		sendReceive.WriteStringVector(_stream,m_vectCCTo);
		sendReceive.WriteStringVector(_stream,m_vectBCCTo);
		sendReceive.WriteStringVector(_stream,m_vectTo);
		sendReceive.WriteStringVector(_stream,m_vectGroup);
		
		sendReceive.WriteString(_stream,m_subject);
		sendReceive.WriteLong(_stream,m_sendDate.getTime());

		sendReceive.WriteInt(_stream,m_flags);
		
		sendReceive.WriteString(_stream,m_XMailName);
		sendReceive.WriteString(_stream,m_contain);
		sendReceive.WriteString(_stream,m_contain_html);
		
		// write the Attachment
		//
		sendReceive.WriteInt(_stream, m_vectAttachment.size());
		for(int i = 0;i < m_vectAttachment.size();i++){
			MailAttachment t_attachment = (MailAttachment)m_vectAttachment.elementAt(i);
			sendReceive.WriteInt(_stream,t_attachment.m_size);
			sendReceive.WriteString(_stream,t_attachment.m_name);
			sendReceive.WriteString(_stream,t_attachment.m_type);
		}
		
		sendReceive.WriteBoolean(_stream,m_hasLocationInfo);
		if(m_hasLocationInfo){
			m_gpsInfo.OutputData(_stream);
		}
		
		sendReceive.WriteString(_stream,m_contain_html_type);
		sendReceive.WriteString(_stream,m_ownAccount);
		
		sendReceive.WriteString(_stream,m_message_id);
		sendReceive.WriteString(_stream,m_in_reply_to);
		sendReceive.WriteString(_stream,m_reference_id);
		
	}
		
	public void InputMail(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();

		m_mailIndex = sendReceive.ReadInt(_stream);		
		
		sendReceive.ReadStringVector(_stream,m_vectFrom);
		sendReceive.ReadStringVector(_stream,m_vectReplyTo);
		sendReceive.ReadStringVector(_stream,m_vectCCTo);
		sendReceive.ReadStringVector(_stream,m_vectBCCTo);
		sendReceive.ReadStringVector(_stream,m_vectTo);
		sendReceive.ReadStringVector(_stream,m_vectGroup);
		
		m_subject = sendReceive.ReadString(_stream);
		m_sendDate.setTime(sendReceive.ReadLong(_stream));
		
		m_flags = sendReceive.ReadInt(_stream);
		
		m_XMailName = sendReceive.ReadString(_stream);
		m_contain = sendReceive.ReadString(_stream);
		m_contain_html = sendReceive.ReadString(_stream);
		
		m_vectAttachment.removeAllElements();
		final int t_attachmentNum = sendReceive.ReadInt(_stream);
		for(int i = 0;i < t_attachmentNum;i++){
			MailAttachment t_attachment = new MailAttachment(); 
			
			t_attachment.m_size = sendReceive.ReadInt(_stream);
			t_attachment.m_name = sendReceive.ReadString(_stream);
			t_attachment.m_type = sendReceive.ReadString(_stream);
			
			m_vectAttachment.addElement(t_attachment);
		}
		
		m_hasLocationInfo = sendReceive.ReadBoolean(_stream);
		if(m_hasLocationInfo){
			m_gpsInfo.InputData(_stream);
		}
		

		if(t_version >= 2){
			m_contain_html_type = sendReceive.ReadString(_stream);
		}
		
		if(t_version >= 3){
			m_ownAccount = sendReceive.ReadString(_stream);
		}
		
		if(t_version >= 4){
			m_message_id = sendReceive.ReadString(_stream);
			m_in_reply_to = sendReceive.ReadString(_stream);
			m_reference_id = sendReceive.ReadString(_stream);
		}
	}
	
	
	
	//set and gets function
	//
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){m_subject = _subject;}
	
	public String GetContain(){return m_contain;}
	public void SetContain(String _contain){m_contain = _contain;}
	
	public String GetContain_html(){return m_contain_html;}
	public void SetContain_html(String _contain_html,String _content_type){
		m_contain_html = _contain_html;
		m_contain_html_type = _content_type;
	}
	public String GetContain_html_type(){return m_contain_html_type;}
	
	public String getOwnAccount(){return m_ownAccount;}
	public void setOwnAccount(String _acc){m_ownAccount = _acc;}
	
	public String GetXMailer(){return m_XMailName;}
	public void SetXMailer(String _str){m_XMailName = _str;}
	
	public String getMessageID(){return m_message_id;}
	public void setMessageID(String _id){m_message_id = _id;}
	
	public String getInReplyTo(){return m_in_reply_to;}
	public void setInReplyTo(String _replyTo){m_in_reply_to = _replyTo;}
	
	public String getReferenceID(){return m_reference_id;}
	public void setReferenceID(String _refID){m_reference_id = _refID;}
	
	public Date GetSendDate(){return m_sendDate;}
	public void SetSendDate(Date _d){m_sendDate = _d;}
	
	public int GetFlags(){return m_flags;}
	public void SetFlags(int _flags){m_flags = _flags;}
	
	public GPSInfo GetGPSInfo(){return m_gpsInfo;}
	
	public void SetSendToVect(String[] _to){
		m_vectTo.removeAllElements();
		for(int i = 0;i < _to.length;i++){
			m_vectTo.addElement(_to[i]);
		}		
	}
	public Vector GetSendToVect(){return m_vectTo;}
	
	public void SetReplyToVect(String[] _replyTo){
		m_vectReplyTo.removeAllElements();
		for(int i = 0;i < _replyTo.length;i++){
			m_vectReplyTo.addElement(_replyTo[i]);
		}		
	}
	public Vector GetReplyToVect(){return m_vectReplyTo;}
	
	public void SetCCToVect(String[] _CCTo){
		m_vectCCTo.removeAllElements();
		for(int i = 0;i < _CCTo.length;i++){
			m_vectCCTo.addElement(_CCTo[i]);
		}		
	}
	public Vector GetCCToVect(){return m_vectCCTo;}
	
	public void SetBCCToVect(String[] _BCCTo){
		m_vectBCCTo.removeAllElements();
		for(int i = 0;i < _BCCTo.length;i++){
			m_vectBCCTo.addElement(_BCCTo[i]);
		}		
	}
	public Vector GetBCCToVect(){return m_vectBCCTo;}
	
	
	public Vector GetFromVect(){return m_vectFrom;}
	public void SetFromVect(String[] _from){
		m_vectFrom.removeAllElements();
		for(int i = 0;i < _from.length;i++){
			m_vectFrom.addElement(_from[i]);
		}		
	}
	
	public Vector GetGroupVect(){return m_vectGroup;}
	public void SetGroupVect(String[] _group){
		m_vectGroup.removeAllElements();
		for(int i = 0;i < _group.length;i++){
			m_vectGroup.addElement(_group[i]);
		}
	}
	
	public void AddAttachment(String _name,String _type,int _size)throws Exception{
		if(_name == null || _name.length() <= 0){
			throw new Exception("Error Attachment format!");
		}
		
		MailAttachment t_attach = new MailAttachment();
		t_attach.m_name = _name;
		t_attach.m_size = _size;
		t_attach.m_type = _type;
		
		m_vectAttachment.addElement(t_attach);
		
	}
	public void ClearAttachment(){
		m_vectAttachment.removeAllElements();
	}
	
	public Vector GetAttachment(){
		return m_vectAttachment;
	}
	
	public void SetLocationInfo(GPSInfo _gpsInfo){
		
		if(_gpsInfo.m_longitude != 0 || _gpsInfo.m_latitude != 0){
			
			m_hasLocationInfo = true;
			
			m_gpsInfo.m_longitude 	= _gpsInfo.m_longitude;
			m_gpsInfo.m_latitude	= _gpsInfo.m_latitude;
			m_gpsInfo.m_altitude	= _gpsInfo.m_altitude;
			m_gpsInfo.m_speed		= _gpsInfo.m_speed;
			m_gpsInfo.m_heading		= _gpsInfo.m_heading;
			
		}else{
			m_hasLocationInfo = false;
		}
	}
	
}


