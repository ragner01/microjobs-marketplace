import React, { useState, useEffect } from 'react';
import { 
  Table, 
  Card, 
  Button, 
  Space, 
  Tag, 
  Input, 
  Select, 
  Modal, 
  Form, 
  InputNumber,
  DatePicker,
  message
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  EyeOutlined
} from '@ant-design/icons';
import axios from 'axios';
import moment from 'moment';

const Jobs = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  });
  const [filters, setFilters] = useState({
    status: '',
    search: ''
  });
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingJob, setEditingJob] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchJobs();
  }, [pagination.current, pagination.pageSize, filters]);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.current - 1,
        size: pagination.pageSize,
        sort: 'createdAt,desc',
        ...filters
      };
      
      const response = await axios.get('/api/jobs', { params });
      setJobs(response.data.content || []);
      setPagination(prev => ({
        ...prev,
        total: response.data.totalElements || 0
      }));
    } catch (error) {
      message.error('Failed to fetch jobs');
      console.error('Error fetching jobs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (paginationInfo) => {
    setPagination(prev => ({
      ...prev,
      current: paginationInfo.current,
      pageSize: paginationInfo.pageSize
    }));
  };

  const handleSearch = (value) => {
    setFilters(prev => ({ ...prev, search: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const handleStatusFilter = (value) => {
    setFilters(prev => ({ ...prev, status: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const showModal = (job = null) => {
    setEditingJob(job);
    setIsModalVisible(true);
    if (job) {
      form.setFieldsValue({
        ...job,
        deadline: job.deadline ? moment(job.deadline) : null
      });
    } else {
      form.resetFields();
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const jobData = {
        ...values,
        deadline: values.deadline ? values.deadline.toISOString() : null
      };

      if (editingJob) {
        await axios.put(`/api/jobs/${editingJob.id}`, jobData);
        message.success('Job updated successfully');
      } else {
        await axios.post('/api/jobs', jobData);
        message.success('Job created successfully');
      }
      
      setIsModalVisible(false);
      fetchJobs();
    } catch (error) {
      message.error('Failed to save job');
      console.error('Error saving job:', error);
    }
  };

  const handleDelete = async (jobId) => {
    Modal.confirm({
      title: 'Are you sure you want to delete this job?',
      content: 'This action cannot be undone.',
      onOk: async () => {
        try {
          await axios.delete(`/api/jobs/${jobId}`);
          message.success('Job deleted successfully');
          fetchJobs();
        } catch (error) {
          message.error('Failed to delete job');
          console.error('Error deleting job:', error);
        }
      }
    });
  };

  const columns = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <a onClick={() => showModal(record)}>{text}</a>
      ),
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
      title: 'Deadline',
      dataIndex: 'deadline',
      key: 'deadline',
      render: (date) => date ? moment(date).format('MMM DD, YYYY') : '-',
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date) => moment(date).format('MMM DD, YYYY'),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EyeOutlined />} 
            onClick={() => showModal(record)}
          >
            View
          </Button>
          <Button 
            type="link" 
            icon={<EditOutlined />} 
            onClick={() => showModal(record)}
          >
            Edit
          </Button>
          <Button 
            type="link" 
            danger 
            icon={<DeleteOutlined />} 
            onClick={() => handleDelete(record.id)}
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card 
        title="Jobs Management"
        extra={
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => showModal()}
          >
            Add Job
          </Button>
        }
      >
        <Space style={{ marginBottom: 16 }}>
          <Input.Search
            placeholder="Search jobs..."
            style={{ width: 300 }}
            onSearch={handleSearch}
            enterButton={<SearchOutlined />}
          />
          <Select
            placeholder="Filter by status"
            style={{ width: 150 }}
            allowClear
            onChange={handleStatusFilter}
          >
            <Select.Option value="OPEN">Open</Select.Option>
            <Select.Option value="ASSIGNED">Assigned</Select.Option>
            <Select.Option value="COMPLETED">Completed</Select.Option>
            <Select.Option value="CANCELLED">Cancelled</Select.Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={jobs}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `${range[0]}-${range[1]} of ${total} jobs`
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title={editingJob ? 'Edit Job' : 'Create Job'}
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={() => setIsModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="Job Title"
            rules={[{ required: true, message: 'Please enter job title' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="description"
            label="Description"
            rules={[{ required: true, message: 'Please enter job description' }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
          
          <Form.Item
            name={['budget', 'amount']}
            label="Budget Amount"
            rules={[{ required: true, message: 'Please enter budget amount' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              formatter={value => `NGN ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => value.replace(/NGN\s?|(,*)/g, '')}
            />
          </Form.Item>
          
          <Form.Item
            name="deadline"
            label="Deadline"
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="location"
            label="Location"
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Jobs;
