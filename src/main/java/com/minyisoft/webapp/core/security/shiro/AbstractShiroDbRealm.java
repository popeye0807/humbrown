package com.minyisoft.webapp.core.security.shiro;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.CollectionUtils;

import com.minyisoft.webapp.core.model.ISystemOrgObject;
import com.minyisoft.webapp.core.model.ISystemRoleObject;
import com.minyisoft.webapp.core.model.ISystemUserObject;
import com.minyisoft.webapp.core.model.PermissionInfo;

/**
 * @author qingyong_ou shiro登录对象
 */
public abstract class AbstractShiroDbRealm<U extends ISystemUserObject,R extends ISystemRoleObject> extends AuthorizingRealm {
	/**
	 * 认证回调函数，登录时调用
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		ISystemUserObject user = getUserByLoginName(token.getUsername());
		if (user != null) {
			if(StringUtils.isBlank(user.getUserPasswordSalt())){
				return new SimpleAuthenticationInfo(createPrincipal(user), user.getUserPassword(),null, getName());
			}else{
				return new SimpleAuthenticationInfo(createPrincipal(user), user.getUserPassword(),ByteSource.Util.bytes(user.getUserPasswordSalt()), getName());
			}
		} else {
			return null;
		}
	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		U user = (U) ((BasePrincipal)principals.getPrimaryPrincipal()).getSystemUser();
		ISystemOrgObject org=getSystemOrg((BasePrincipal)principals.getPrimaryPrincipal());
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		List<R> userRoles = getUserRoles(user,org);
		if (!CollectionUtils.isEmpty(userRoles)) {
			for (R role : userRoles) {
				info.addRole(role.getValue());
			}
		}
		List<PermissionInfo> userPermissions = getUserPermissions(user,org);
		if (!CollectionUtils.isEmpty(userPermissions)) {
			for (PermissionInfo permission : userPermissions) {
				info.addStringPermission(permission.getValue());
			}
		}
		return info;
	}

	/**
	 * 设定Password校验的Hash算法与迭代次数.
	 */
	@PostConstruct
	public void initCredentialsMatcher() {
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(getHashAlgorithm());
		matcher.setHashIterations(getHashInterations()<=0?1024:getHashInterations());

		setCredentialsMatcher(matcher);
	}
	
	/**
	 * 创建Principal，业务系统可根据实际需求返回继承BasePrincipal的Principal
	 * @param user
	 * @return
	 */
	public abstract BasePrincipal createPrincipal(ISystemUserObject user);

	/**
	 * 获取哈希算法
	 * 
	 * @return
	 */
	public abstract String getHashAlgorithm();

	/**
	 * 获取哈希迭代次数
	 * 
	 * @return
	 */
	public abstract int getHashInterations();
	
	/**
	 * 获取登录用户所在组织架构
	 * @param basePrincipal
	 * @return
	 */
	public abstract ISystemOrgObject getSystemOrg(BasePrincipal basePrincipal);

	/**
	 * 根据登录名获取用户信息
	 * 
	 * @param userLoginName
	 * @return
	 */
	public abstract U getUserByLoginName(String userLoginName);

	/**
	 * 获取授予指定用户于指定组织结构的角色列表
	 * @param user
	 * @param systemOrg 可为空
	 * @return
	 */
	public abstract List<R> getUserRoles(U user,ISystemOrgObject systemOrg);

	/**
	 * 获取授予指定用户于指定组织结构的权限列表
	 * @param user
	 * @param systemOrg 可为空
	 * @return
	 */
	public abstract List<PermissionInfo> getUserPermissions(U user,ISystemOrgObject systemOrg);
}
