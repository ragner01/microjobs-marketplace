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
  DollarOutlined
} from '@ant-design/icons';
import axios from 'axios';

const Escrow = () => {
  const [transactions, setTransactions] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  });
  const [filters, setFilters] = useState({
    status: '',
    type: ''
  });
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState(null);

  useEffect(() => {
    fetchTransactions();
    fetchAccounts();
  }, [pagination.current, pagination.pageSize, filters]);

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.current - 1,
        size: pagination.pageSize,
        sort: 'initiatedAt,desc',
        ...filters
      };
      
      const response = await axios.get('/api/escrow/transactions', { params });
      setTransactions(response.data.content || []);
      setPagination(prev => ({
        ...prev,
        total: response.data.totalElements || 0
      }));
    } catch (error) {
      message.error('Failed to fetch transactions');
      console.error('Error fetching transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await axios.get('/api/escrow/accounts');
      setAccounts(response.data || []);
    } catch (error) {
      console.error('Error fetching accounts:', error);
    }
  };

  const handleTableChange = (paginationInfo) => {
    setPagination(prev => ({
      ...prev,
      current: paginationInfo.current,
      pageSize: paginationInfo.pageSize
    }));
  };

  const handleStatusFilter = (value) => {
    setFilters(prev => ({ ...prev, status: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const handleTypeFilter = (value) => {
    setFilters(prev => ({ ...prev, type: value }));
    setPagination(prev => ({ ...prev, current: 1 }));
  };

  const showTransactionDetails = async (transaction) => {
    try {
      const response = await axios.get(`/api/escrow/transactions/${transaction.id}`);
      setSelectedTransaction(response.data);
      setIsModalVisible(true);
    } catch (error) {
      message.error('Failed to fetch transaction details');
      console.error('Error fetching transaction details:', error);
    }
  };

  const handleReleasePayment = async (transactionId) => {
    Modal.confirm({
      title: 'Release Payment',
      content: 'Are you sure you want to release this payment to the worker?',
      onOk: async () => {
        try {
          await axios.post(`/api/escrow/transactions/${transactionId}/release`);
          message.success('Payment released successfully');
          fetchTransactions();
        } catch (error) {
          message.error('Failed to release payment');
          console.error('Error releasing payment:', error);
        }
      }
    });
  };

  const handleRefundPayment = async (transactionId) => {
    Modal.confirm({
      title: 'Refund Payment',
      content: 'Are you sure you want to refund this payment to the client?',
      onOk: async () => {
        try {
          await axios.post(`/api/escrow/transactions/${transactionId}/refund`);
          message.success('Payment refunded successfully');
          fetchTransactions();
        } catch (error) {
          message.error('Failed to refund payment');
          console.error('Error refunding payment:', error);
        }
      }
    });
  };

  const columns = [
    {
      title: 'Transaction ID',
      dataIndex: 'id',
      key: 'id',
      render: (text) => <a>{text.substring(0, 8)}...</a>,
    },
    {
      title: 'Job ID',
      dataIndex: 'jobId',
      key: 'jobId',
      render: (text) => text ? text.substring(0, 8) + '...' : '-',
    },
    {
      title: 'Client',
      dataIndex: 'clientId',
      key: 'clientId',
    },
    {
      title: 'Worker',
      dataIndex: 'workerId',
      key: 'workerId',
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => `${amount.currency} ${amount.amount.toLocaleString()}`,
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type) => {
        const color = {
          'JOB_PAYMENT': 'blue',
          'DISPUTE_REFUND': 'orange',
          'PLATFORM_FEE': 'green',
          'PENALTY': 'red'
        }[type] || 'default';
        return <Tag color={color}>{type.replace('_', ' ')}</Tag>;
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const color = {
          'PENDING': 'orange',
          'COMPLETED': 'green',
          'FAILED': 'red',
          'CANCELLED': 'gray'
        }[status] || 'default';
        return <Tag color={color}>{status}</Tag>;
      },
    },
    {
      title: 'Initiated',
      dataIndex: 'initiatedAt',
      key: 'initiatedAt',
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
            onClick={() => showTransactionDetails(record)}
          >
            View
          </Button>
          {record.status === 'PENDING' && record.type === 'JOB_PAYMENT' && (
            <>
              <Button 
                type="link" 
                icon={<DollarOutlined />} 
                onClick={() => handleReleasePayment(record.id)}
              >
                Release
              </Button>
              <Button 
                type="link" 
                danger 
                onClick={() => handleRefundPayment(record.id)}
              >
                Refund
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card title="Escrow Management">
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="Filter by status"
            style={{ width: 150 }}
            allowClear
            onChange={handleStatusFilter}
          >
            <Select.Option value="PENDING">Pending</Select.Option>
            <Select.Option value="COMPLETED">Completed</Select.Option>
            <Select.Option value="FAILED">Failed</Select.Option>
            <Select.Option value="CANCELLED">Cancelled</Select.Option>
          </Select>
          <Select
            placeholder="Filter by type"
            style={{ width: 150 }}
            allowClear
            onChange={handleTypeFilter}
          >
            <Select.Option value="JOB_PAYMENT">Job Payment</Select.Option>
            <Select.Option value="DISPUTE_REFUND">Dispute Refund</Select.Option>
            <Select.Option value="PLATFORM_FEE">Platform Fee</Select.Option>
            <Select.Option value="PENALTY">Penalty</Select.Option>
          </Select>
        </Space>

        <Table
          columns={columns}
          dataSource={transactions}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `${range[0]}-${range[1]} of ${total} transactions`
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title="Transaction Details"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedTransaction && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="Transaction ID" span={2}>
              {selectedTransaction.id}
            </Descriptions.Item>
            <Descriptions.Item label="Job ID">
              {selectedTransaction.jobId}
            </Descriptions.Item>
            <Descriptions.Item label="Type">
              <Tag color="blue">{selectedTransaction.type}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Client ID">
              {selectedTransaction.clientId}
            </Descriptions.Item>
            <Descriptions.Item label="Worker ID">
              {selectedTransaction.workerId}
            </Descriptions.Item>
            <Descriptions.Item label="Amount">
              {selectedTransaction.amount.currency} {selectedTransaction.amount.amount.toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color="green">{selectedTransaction.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Description" span={2}>
              {selectedTransaction.description}
            </Descriptions.Item>
            <Descriptions.Item label="Initiated At">
              {new Date(selectedTransaction.initiatedAt).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Completed At">
              {selectedTransaction.completedAt 
                ? new Date(selectedTransaction.completedAt).toLocaleString() 
                : '-'
              }
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Escrow;
