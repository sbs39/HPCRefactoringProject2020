#include <stdio.h>
#include <mpi.h>

int main(argc, argv)
	int argc;char *argv[1]; {
	MPI_Request newRequest;
	int numtasks, rank, dest, source, rc, count, tag = 1;
	int num1, num2;
	char inmsg, inmsg123, outmsg = 'x', temp;

	MPI_Status stat;

	MPI_Init(&argc, &argv);

	MPI_Comm_size(MPI_COMM_WORLD, &numtasks);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	printf("numtasks: %d\n", numtasks);
	printf("rank: %d\n", rank);

	if (rank == 0) {
		dest = 1;
		source = 1;
		temp = 1;
		rc = MPI_Send(&outmsg, 1, MPI_CHAR, dest, tag, MPI_COMM_WORLD);
		printf("Message has been sent");
		num1 = source;
		num1 = num1 + 123;
		temp = (char) num1;
	} else if (rank == 1) {
		dest = 0;
		source = 0;
		rc = MPI_Irecv(&inmsg, 1, MPI_CHAR, source, tag, MPI_COMM_WORLD,
				&newRequest);
		printf("Message has been received");
		num2 = source;
		MPI_Wait(&newRequest, &stat);
		inmsg = "123";
		num2 = source + 123;
		temp = (char) num2;
	}

	rc = MPI_Get_count(&stat, MPI_CHAR, &count);
	outmsg = temp;
	printf("outmsg %c", outmsg);

	MPI_Finalize();
}
