#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<termios.h>
#include<errno.h>
#include<string.h>
#include<sys/socket.h>
#include<netinet/in.h>
#define FALSE -1
#define TRUE 0
/*

 * 串口配置 波特率，停止位，数据位，校验位

 */
int fd;
int speed_arr[] = {B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300};
long name_arr[] = {115200, 38400,19200,9600,4800,2400,1200,300};
void set_speed(int fd, long speed)
{
	int i;
	int status;
	struct termios Opt;
	tcgetattr(fd, &Opt);

	for (i= 0;i < sizeof(speed_arr) / sizeof(int);i++) {
		if(speed == name_arr[i]) {
			tcflush(fd, TCIOFLUSH);
			cfsetispeed(&Opt, speed_arr[i]);
			cfsetospeed(&Opt, speed_arr[i]);
			status = tcsetattr(fd, TCSANOW, &Opt);
			if(status != 0) {
				printf("tcsetattr fd\n");
				return;
			}
			tcflush(fd,TCIOFLUSH);
		}
	}
}

int set_Parity(int fd,int databits,int stopbits,int parity)
{
	struct termios options;
	if( tcgetattr( fd,&options)!=0) {
		perror("SetupSerial 1");
		return(FALSE);
	}
	options.c_cflag &= ~CSIZE;
	switch (databits)
	{
	case 7:
		options.c_cflag |= CS7;
		break;
	case 8:
		options.c_cflag |= CS8;
		break;
	default:
		fprintf(stderr,"Unsupported data size\n");
		return (FALSE);
	}
	switch (parity)
	{
	case 'n':
	case 'N': //无校验位
		options.c_cflag &= ~PARENB; /* Clear parity enable */
		options.c_iflag &= ~INPCK; /* Enable parity checking */
		break;
	case 'o':
	case 'O':
		options.c_cflag |= (PARODD | PARENB); //奇校验
		options.c_iflag |= INPCK; /* Disnable parity checking */
		break;
	case 'e':
	case 'E':
		options.c_cflag |= PARENB; /* Enable parity */
		options.c_cflag &= ~PARODD;//偶校验
		options.c_iflag |= INPCK; /* Disnable parity checking */
		break;
	case 'S':
	case 's':/*as no parity*/
		options.c_cflag &= ~PARENB;
		options.c_cflag &= ~CSTOPB;break;
	default:
		fprintf(stderr,"Unsupported parity\n");
		return (FALSE);
	}

	switch (stopbits)
	{
	case 1:
		options.c_cflag &= ~CSTOPB;
		break;
	case 2:
		options.c_cflag |= CSTOPB;
		break;
	default:
		fprintf(stderr,"Unsupported stop bits\n");
		return (FALSE);
	}
	/* Set input parity option */
	if (parity != 'n')
		options.c_iflag |= INPCK;
	tcflush(fd,TCIFLUSH);
	options.c_cc[VTIME] = 0; //read 0s//读取的等待时间，一般为0
	options.c_cc[VMIN] = 0; /* Update the options and do it NOW */
	if (tcsetattr(fd,TCSANOW,&options) != 0)
	{
		perror("SetupSerial 3");
		return (FALSE);
	}
	return (TRUE);
}

int ttyUSBOpen(void)
{
	fd = open("/dev/ttyUSB0",O_RDWR|O_NOCTTY|O_NDELAY);

	if(fd < 0)
	{
		printf("open device error!\n");
		return -1;
	}
	printf("open ttyUSB0 succesfully fd = %d\n", fd);
	set_speed(fd, 115200);
	if(set_Parity(fd, 8, 1, 'N') == FALSE)
	{
		printf("Set Parity Error\n");
		return -2;
	}
	return fd;
}
int ttyUSBClose(void)
{
	close(fd);
	return 0;
}

int ttyUSBWrite(void *buf, int buf_len)
{
	int len = 0;
	len = write(fd, buf, buf_len);
	if (len == buf_len)
	{
		return len;
	}
	else
		return -1;
}

int ttyUSBRead(void *buf, int buf_size)
{
	int len = 0;
	len = read(fd,buf,buf_size);
	return  len;
}
