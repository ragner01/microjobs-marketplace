import React, { useState, useEffect } from 'react';
import { Layout, Menu, Avatar, Dropdown, Space, Badge } from 'antd';
import { 
  DashboardOutlined, 
  TeamOutlined, 
  FileTextOutlined, 
  DollarOutlined, 
  BarChartOutlined,
  UserOutlined,
  BellOutlined,
  LogoutOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';

const { Sider } = Layout;

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const [notifications, setNotifications] = useState(5);

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/tenants',
      icon: <TeamOutlined />,
      label: 'Tenants',
    },
    {
      key: '/jobs',
      icon: <FileTextOutlined />,
      label: 'Jobs',
    },
    {
      key: '/escrow',
      icon: <DollarOutlined />,
      label: 'Escrow',
    },
    {
      key: '/analytics',
      icon: <BarChartOutlined />,
      label: 'Analytics',
    },
  ];

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
    },
    {
      key: 'settings',
      icon: <UserOutlined />,
      label: 'Settings',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
    },
  ];

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  const handleUserMenuClick = ({ key }) => {
    if (key === 'logout') {
      // Handle logout
      console.log('Logout clicked');
    }
  };

  return (
    <Sider 
      collapsible 
      collapsed={collapsed} 
      onCollapse={setCollapsed}
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
      }}
    >
      <div style={{ 
        height: 32, 
        margin: 16, 
        background: 'rgba(255, 255, 255, 0.2)',
        borderRadius: 6,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white',
        fontWeight: 'bold'
      }}>
        {collapsed ? 'MJ' : 'MicroJobs'}
      </div>
      
      <Menu
        theme="dark"
        defaultSelectedKeys={[location.pathname]}
        mode="inline"
        items={menuItems}
        onClick={handleMenuClick}
      />
      
      <div style={{ 
        position: 'absolute', 
        bottom: 16, 
        left: 16, 
        right: 16 
      }}>
        <Dropdown
          menu={{
            items: userMenuItems,
            onClick: handleUserMenuClick,
          }}
          placement="topRight"
        >
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            padding: '8px 12px',
            background: 'rgba(255, 255, 255, 0.1)',
            borderRadius: 6,
            cursor: 'pointer',
            color: 'white'
          }}>
            <Space>
              <Avatar size="small" icon={<UserOutlined />} />
              {!collapsed && (
                <>
                  <span>Admin User</span>
                  <Badge count={notifications} size="small">
                    <BellOutlined />
                  </Badge>
                </>
              )}
            </Space>
          </div>
        </Dropdown>
      </div>
    </Sider>
  );
};

export default Sidebar;
