import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  Table, 
  Tag, 
  Space, 
  Select,
  DatePicker,
  Button
} from 'antd';
import { 
  DollarOutlined, 
  TeamOutlined, 
  FileTextOutlined, 
  CheckCircleOutlined,
  ReloadOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import axios from 'axios';
import moment from 'moment';

const Analytics = () => {
  const [stats, setStats] = useState({
    totalJobs: 0,
    activeJobs: 0,
    completedJobs: 0,
    totalRevenue: 0,
    totalTenants: 0,
    totalWorkers: 0,
    averageJobValue: 0,
    completionRate: 0
  });
  const [jobTrends, setJobTrends] = useState([]);
  const [revenueTrends, setRevenueTrends] = useState([]);
  const [topSkills, setTopSkills] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState([moment().subtract(30, 'days'), moment()]);

  useEffect(() => {
    fetchAnalyticsData();
  }, [dateRange]);

  const fetchAnalyticsData = async () => {
    setLoading(true);
    try {
      const params = {
        startDate: dateRange[0].format('YYYY-MM-DD'),
        endDate: dateRange[1].format('YYYY-MM-DD')
      };

      // Fetch statistics
      const statsResponse = await axios.get('/api/analytics/dashboard-stats', { params });
      setStats(statsResponse.data);

      // Fetch job trends
      const trendsResponse = await axios.get('/api/analytics/job-trends', { params });
      setJobTrends(trendsResponse.data);

      // Fetch revenue trends
      const revenueResponse = await axios.get('/api/analytics/revenue-trends', { params });
      setRevenueTrends(revenueResponse.data);

      // Fetch top skills
      const skillsResponse = await axios.get('/api/analytics/top-skills', { params });
      setTopSkills(skillsResponse.data);

    } catch (error) {
      console.error('Error fetching analytics data:', error);
    } finally {
      setLoading(false);
    }
  };

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  const skillsColumns = [
    {
      title: 'Skill',
      dataIndex: 'skill',
      key: 'skill',
    },
    {
      title: 'Job Count',
      dataIndex: 'count',
      key: 'count',
      render: (count) => <Tag color="blue">{count}</Tag>,
    },
    {
      title: 'Average Budget',
      dataIndex: 'averageBudget',
      key: 'averageBudget',
      render: (amount) => `NGN ${amount.toLocaleString()}`,
    },
  ];

  return (
    <div>
      <Card 
        title="Analytics Dashboard"
        extra={
          <Space>
            <DatePicker.RangePicker
              value={dateRange}
              onChange={setDateRange}
            />
            <Button 
              icon={<ReloadOutlined />} 
              onClick={fetchAnalyticsData}
              loading={loading}
            >
              Refresh
            </Button>
          </Space>
        }
      >
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
                title="Completion Rate"
                value={stats.completionRate}
                suffix="%"
                prefix={<BarChartOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>

        {/* Charts Row */}
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={12}>
            <Card title="Job Trends">
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={jobTrends}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="jobs" stroke="#8884d8" />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col span={12}>
            <Card title="Revenue Trends">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={revenueTrends}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="revenue" fill="#82ca9d" />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
        </Row>

        {/* Top Skills Table */}
        <Card title="Top Skills">
          <Table
            columns={skillsColumns}
            dataSource={topSkills}
            rowKey="skill"
            pagination={false}
            loading={loading}
          />
        </Card>
      </Card>
    </div>
  );
};

export default Analytics;
