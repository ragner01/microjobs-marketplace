import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Tag, Button, Space, Input, Select } from 'antd';
import { 
  DollarOutlined, 
  TeamOutlined, 
  FileTextOutlined, 
  CheckCircleOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import axios from 'axios';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalJobs: 0,
    activeJobs: 0,
    completedJobs: 0,
    totalRevenue: 0,
    totalTenants: 0,
    totalWorkers: 0
  });
  const [recentJobs, setRecentJobs] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // Fetch statistics
      const statsResponse = await axios.get('/api/analytics/dashboard-stats');
      setStats(statsResponse.data);

      // Fetch recent jobs
      const jobsResponse = await axios.get('/api/jobs?page=0&size=10&sort=createdAt,desc');
      setRecentJobs(jobsResponse.data.content || []);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: 'Job Title',
      dataIndex: 'title',
      key: 'title',
      render: (text) => <a>{text}</a>,
    },
    {
      title: 'Client',
      dataIndex: 'clientId',
      key: 'clientId',
    },
    {
      title: 'Budget',
      dataIndex: 'budget',
      key: 'budget',
      render: (budget) => `${budget.currency} ${budget.amount.toLocaleString()}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const color = {
          'OPEN': 'blue',
          'ASSIGNED': 'orange',
          'COMPLETED': 'green',
          'CANCELLED': 'red'
        }[status] || 'default';
        return <Tag color={color}>{status}</Tag>;
      },
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date) => new Date(date).toLocaleDateString(),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button type="link" size="small">View</Button>
          <Button type="link" size="small">Edit</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h1>Dashboard</h1>
      
      {/* Statistics Cards */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Jobs"
              value={stats.totalJobs}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Active Jobs"
              value={stats.activeJobs}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Revenue"
              value={stats.totalRevenue}
              prefix={<DollarOutlined />}
              suffix="NGN"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Tenants"
              value={stats.totalTenants}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Recent Jobs Table */}
      <Card 
        title="Recent Jobs" 
        extra={
          <Button 
            icon={<ReloadOutlined />} 
            onClick={fetchDashboardData}
            loading={loading}
          >
            Refresh
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={recentJobs}
          rowKey="id"
          pagination={false}
          loading={loading}
        />
      </Card>
    </div>
  );
};

export default Dashboard;
