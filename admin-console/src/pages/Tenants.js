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
  message,
  Descriptions
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  EyeOutlined,
  TeamOutlined
} from '@ant-design/icons';
import axios from 'axios';

const Tenants = () => {
  const [tenants, setTenants] = useState([]);
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
  const [editingTenant, setEditingTenant] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchTenants();
  }, [pagination.current, pagination.pageSize, filters]);

  const fetchTenants = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.current - 1,
        size: pagination.pageSize,
        sort: 'createdAt,desc',
        ...filters
      };
      
      const response = await axios.get('/api/tenants', { params });
      setTenants(response.data.content || []);
      setPagination(prev => ({
        ...prev,
        total: response.data.totalElements || 0
      }));
    } catch (error) {
      message.error('Failed to fetch tenants');
      console.error('Error fetching tenants:', error);
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

  const showModal = (tenant = null) => {
    setEditingTenant(tenant);
    setIsModalVisible(true);
    if (tenant) {
      form.setFieldsValue(tenant);
    } else {
      form.resetFields();
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingTenant) {
        await axios.put(`/api/tenants/${editingTenant.id}`, values);
        message.success('Tenant updated successfully');
      } else {
        await axios.post('/api/tenants', values);
        message.success('Tenant created successfully');
      }
      
      setIsModalVisible(false);
      fetchTenants();
    } catch (error) {
      message.error('Failed to save tenant');
      console.error('Error saving tenant:', error);
    }
  };

  const handleDelete = async (tenantId) => {
    Modal.confirm({
      title: 'Are you sure you want to delete this tenant?',
      content: 'This action cannot be undone and will affect all associated data.',
      onOk: async () => {
        try {
          await axios.delete(`/api/tenants/${tenantId}`);
          message.success('Tenant deleted successfully');
          fetchTenants();
        } catch (error) {
          message.error('Failed to delete tenant');
          console.error('Error deleting tenant:', error);
        }
      }
    });
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <a onClick={() => showModal(record)}>{text}</a>
      ),
    },
    {
      title: 'Domain',
      dataIndex: 'domain',
      key: 'domain',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const color = {
          'ACTIVE': 'green',
          'SUSPENDED': 'orange',
          'INACTIVE': 'red'
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
        title="Tenants Management"
        extra={
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => showModal()}
          >
            Add Tenant
          </Button>
        }
      >
        <Space style={{ marginBottom: 16 }}>
          <Input.Search
            placeholder="Search tenants..."
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
            <Select.Option value="ACTIVE">Active</Select.Option>
            <Select.Option value="SUSPENDED">Suspended</Select.Option>
            <Select.Option value="INACTIVE">Inactive</Select.Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={tenants}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `${range[0]}-${range[1]} of ${total} tenants`
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title={editingTenant ? 'Edit Tenant' : 'Create Tenant'}
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={() => setIsModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="Tenant Name"
            rules={[{ required: true, message: 'Please enter tenant name' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="domain"
            label="Domain"
            rules={[{ required: true, message: 'Please enter domain' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="status"
            label="Status"
            rules={[{ required: true, message: 'Please select status' }]}
          >
            <Select>
              <Select.Option value="ACTIVE">Active</Select.Option>
              <Select.Option value="SUSPENDED">Suspended</Select.Option>
              <Select.Option value="INACTIVE">Inactive</Select.Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name={['settings', 'currency']}
            label="Default Currency"
          >
            <Select>
              <Select.Option value="NGN">NGN</Select.Option>
              <Select.Option value="USD">USD</Select.Option>
              <Select.Option value="EUR">EUR</Select.Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            name={['settings', 'timezone']}
            label="Timezone"
          >
            <Select>
              <Select.Option value="Africa/Lagos">Africa/Lagos</Select.Option>
              <Select.Option value="UTC">UTC</Select.Option>
              <Select.Option value="America/New_York">America/New_York</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Tenants;
